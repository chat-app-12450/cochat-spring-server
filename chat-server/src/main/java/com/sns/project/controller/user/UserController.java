package com.sns.project.controller.user;

import com.sns.project.controller.user.dto.request.RequestRegisterDto;
import com.sns.project.controller.user.dto.request.LoginRequestDto;
import com.sns.project.controller.user.dto.request.RequestPasswordResetDto;
import com.sns.project.controller.user.dto.request.ResetPasswordDto;
import com.sns.project.controller.user.dto.response.ResponseUserDto;
import com.sns.project.domain.user.User;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.service.user.TokenService;
import com.sns.project.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

  private final TokenService tokenService;
  private final UserService userService;
  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  @PostMapping("/register")
  public ApiResult<String> register(@RequestBody RequestRegisterDto request) {
    userService.register(request);
    return ApiResult.success("회원가입 성공 :" + request.getEmail());
  }

  @Operation(summary = "로그인", description = "사용자 ID와 비밀번호로 로그인합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "로그인 성공"),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @PostMapping("/login")
  public ApiResult<String> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
    String token = userService.authenticate(request.getUserId(), request.getPassword());
    Cookie cookie = new Cookie("token", token);
    cookie.setPath("/");
    cookie.setHttpOnly(false);
    cookie.setSecure(false);
//    cookie.setMaxAge(3600);
    response.addCookie(cookie);
    return ApiResult.success("login success");
  }

  @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃합니다")
  @ApiResponse(responseCode = "200", description = "로그아웃 성공")
  @PostMapping("/logout")
  public void logout() {
  }

  @Operation(summary = "비밀번호 재설정 요청", 
            description = "이메일로 비밀번호 재설정 링크를 발송합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "재설정 링크 발송 성공"),
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  @PostMapping("/request-reset-password")
  public ApiResult<String> requestPasswordReset(@RequestBody RequestPasswordResetDto request) {
    userService.requestPasswordReset(request.getEmail());
    return ApiResult.success("비밀번호 재생성 링크를 보냈습니다.");
  }

  @Operation(summary = "비밀번호 재설정", 
            description = "토큰을 확인하고 새로운 비밀번호로 변경합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 토큰"),
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  @PostMapping("/reset-password")
  public ApiResult<String> resetPassword(@RequestParam String token, 
                                       @RequestBody ResetPasswordDto request) {
    userService.resetPassword(token, request.getNewPassword());
    return ApiResult.success("비밀번호가 성공적으로 재설정되었습니다.");
  }

  @Operation(summary = "토큰 유효성 검사", description = "유저의 토큰이 올바른지 확인합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "토큰이 유효함"),
    @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않음")
  })
  @PostMapping("/validate-token")
public ApiResult<ResponseUserDto> validateToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    String token = null;
    
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("token".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }

    Long userId = tokenService.validateToken(token);
    User user = userService.getUserById(userId);
    ResponseUserDto responseUserDto = new ResponseUserDto(user);
    return ApiResult.success(responseUserDto);
  }

  @Operation(summary = "사용자 검색", description = "사용자 ID로 사용자를 검색합니다")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "사용자 검색 성공"),
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  @PostMapping("/search")
  public ApiResult<String> searchUser(@RequestParam String userId) {
   userService.findByUserId(userId);
    return ApiResult.success("사용자를 찾았습니다: " + userId);
  }
}
