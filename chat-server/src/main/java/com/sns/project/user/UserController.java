package com.sns.project.user;

import com.sns.project.auth.AuthRequired;
import com.sns.project.auth.AuthCookieService;
import com.sns.project.auth.UserContext;
import com.sns.project.user.dto.request.RequestRegisterDto;
import com.sns.project.user.dto.request.LoginRequestDto;
import com.sns.project.user.dto.request.RequestPasswordResetDto;
import com.sns.project.user.dto.request.ResetPasswordDto;
import com.sns.project.user.dto.request.VerifyUserLocationRequest;
import com.sns.project.user.dto.response.ResponseUserDto;
import com.sns.project.user.dto.response.UserVerifiedLocationResponse;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.exception.unauthorized.UnauthorizedException;
import com.sns.project.common.api.ApiResult;
import com.sns.project.auth.TokenService;
import com.sns.project.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
@Validated
/*
 * 사용자 인증 흐름
 *
 * 1. 회원가입(register)
 * - 계정만 생성한다.
 * - 토큰이나 쿠키는 발급하지 않는다.
 *
 * 2. 로그인(login)
 * - access token 과 refresh token 을 함께 발급한다.
 * - access token 은 짧게 쓰고, refresh token 은 Redis 세션 저장소와 함께 관리한다.
 * - 브라우저에는 ACCESS_TOKEN / REFRESH_TOKEN / XSRF-TOKEN 쿠키를 내려준다.
 *
 * 3. 로그아웃(logout)
 * - refresh token 세션을 Redis 에서 제거한다.
 * - 브라우저 쿠키는 즉시 만료시킨다.
 * - access token 은 짧은 TTL 후 자연 만료된다.
 *
 * 4. 재발급(refresh)
 * - refresh token 의 JWT 검증 + Redis 세션 검증을 통과해야 한다.
 * - 통과하면 새 access / refresh 쌍을 다시 발급한다.
 */
public class UserController {

  private final TokenService tokenService;
  private final AuthCookieService authCookieService;
  private final UserService userService;
  private final UserLocationVerificationService userLocationVerificationService;
  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @PostMapping("/register")
  public ApiResult<String> register(@Valid @RequestBody RequestRegisterDto request) {
    userService.register(request);
    return ApiResult.success("회원가입 성공 :" + request.getEmail());
  }

  @Operation(summary = "로그인", description = "사용자 ID와 비밀번호로 로그인합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "로그인 성공"),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @PostMapping("/login")
  public ApiResult<ResponseUserDto> login(@Valid @RequestBody LoginRequestDto request, HttpServletResponse response) {
    User user = userService.authenticate(request.getUserId(), request.getPassword());
    TokenService.TokenPair tokenPair = tokenService.issueTokens(user.getId());
    // 로그인 시 인증 쿠키와 CSRF 쿠키를 함께 내려 이후 상태 변경 요청을 보호한다.
    authCookieService.writeAuthenticationCookies(response, tokenPair.accessToken(), tokenPair.refreshToken());
    // 인증 응답이 브라우저/프록시 캐시에 남지 않게 한다.
    response.setHeader("Cache-Control", "no-store");
    return ApiResult.success(new ResponseUserDto(user));
  }

  @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃합니다")
  @ApiResponse(responseCode = "200", description = "로그아웃 성공")
  @PostMapping("/logout")
  public ApiResult<String> logout(HttpServletRequest request, HttpServletResponse response) {
    authCookieService.extractRefreshToken(request).ifPresent(tokenService::revokeRefreshTokenSilently);
    // 로그아웃은 서버 상태보다 브라우저 인증 상태 정리가 핵심이므로 쿠키를 즉시 만료시킨다.
    authCookieService.clearAuthenticationCookies(response);
    response.setHeader("Cache-Control", "no-store");
    return ApiResult.success("logout success");
  }




  @Operation(summary = "토큰 유효성 검사", description = "유저의 토큰이 올바른지 확인합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "토큰이 유효함"),
    @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않음")
  })
  @GetMapping("/validate-token")
public ApiResult<ResponseUserDto> validateToken(HttpServletRequest request) {
    // Access token은 HttpOnly 쿠키에만 있으므로 서버가 직접 꺼내서 검증한다.
    String token = authCookieService.extractAccessToken(request)
        .orElseThrow(() -> new UnauthorizedException("인증 토큰이 없습니다."));

    Long userId = tokenService.validateToken(token);
    User user = userService.getUserById(userId);
    ResponseUserDto responseUserDto = new ResponseUserDto(user);
    return ApiResult.success(responseUserDto);
  }

  @Operation(summary = "토큰 재발급", description = "refresh token으로 인증 쿠키를 재발급합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "재발급 성공"),
    @ApiResponse(responseCode = "401", description = "refresh token이 유효하지 않음")
  })
  @PostMapping("/refresh")
  public ApiResult<String> refresh(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = authCookieService.extractRefreshToken(request)
        .orElseThrow(() -> new UnauthorizedException("리프레시 토큰이 없습니다."));

    Long userId = tokenService.validateRefreshToken(refreshToken);
    tokenService.revokeRefreshToken(refreshToken);

    TokenService.TokenPair tokenPair = tokenService.issueTokens(userId);
    authCookieService.writeAuthenticationCookies(response, tokenPair.accessToken(), tokenPair.refreshToken());
    response.setHeader("Cache-Control", "no-store");
    return ApiResult.success("refresh success");
  }

  @Operation(summary = "사용자 검색", description = "사용자 ID로 사용자를 검색합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "사용자 검색 성공"),
    @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  @PostMapping("/search")
  @AuthRequired
  public ApiResult<String> searchUser(@RequestParam @NotBlank(message = "userId는 비어 있을 수 없습니다.") @Size(max = 30, message = "userId는 30자 이하여야 합니다.") String userId) {
   userService.findByUserId(userId);
    return ApiResult.success("사용자를 찾았습니다: " + userId);
  }

  @PostMapping("/location/verify")
  @AuthRequired
  public ApiResult<UserVerifiedLocationResponse> verifyLocation(@Valid @RequestBody VerifyUserLocationRequest request) {
    Long userId = UserContext.getUserId();
    return ApiResult.success(userLocationVerificationService.verifyLocation(userId, request));
  }

  @GetMapping("/location")
  @AuthRequired
  public ApiResult<UserVerifiedLocationResponse> getVerifiedLocation() {
    Long userId = UserContext.getUserId();
    return ApiResult.success(userLocationVerificationService.getCurrentVerifiedLocation(userId));
  }
}
