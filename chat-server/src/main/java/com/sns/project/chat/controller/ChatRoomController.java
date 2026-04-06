package com.sns.project.chat.controller;

import com.sns.project.auth.AuthRequired;
import com.sns.project.auth.UserContext;
import com.sns.project.chat.controller.dto.request.OpenGroupRoomCreateRequest;
import com.sns.project.chat.controller.dto.request.RoomCreationRequest;
import com.sns.project.chat.controller.dto.response.RoomListResponse;
import com.sns.project.chat.controller.dto.response.ChatHistoryResponse;
import com.sns.project.chat.controller.dto.response.ChatHistoryPageResponse;
import com.sns.project.chat.controller.dto.response.RoomInfoResponse;
import com.sns.project.core.domain.user.User;
import com.sns.project.common.api.ApiResult;
import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.user.UserService;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Validated
//@CrossOrigin(origins = "http://localhost:3000")  // Allow requests from frontend

public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final UserService userService;

    @PostMapping("/room")
    @AuthRequired
    public ApiResult<RoomInfoResponse> createRoom(@Valid @RequestBody RoomCreationRequest roomCreationRequest) {
        Long userId = UserContext.getUserId();
        User creator = userService.getUserById(userId);

        return ApiResult.success(chatRoomService.createRoom(
            roomCreationRequest.getName(),
            roomCreationRequest.getUserIds(),
            creator
        ));
    }

    @PostMapping("/open-rooms")
    @AuthRequired
    public ApiResult<RoomInfoResponse> createOpenGroupRoom(@Valid @RequestBody OpenGroupRoomCreateRequest request) {
        Long userId = UserContext.getUserId();
        User creator = userService.getUserById(userId);
        return ApiResult.success(chatRoomService.createOpenGroupRoom(
            request.getName(),
            request.getDescription(),
            request.getMaxParticipants(),
            creator
        ));
    }

    @GetMapping("/open-rooms")
    @AuthRequired
    public ApiResult<RoomListResponse> searchOpenGroupRooms(
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "latitude", required = false) Double latitude,
        @RequestParam(name = "longitude", required = false) Double longitude,
        @RequestParam(name = "radius_km", required = false) @Positive(message = "radius_km는 0보다 커야 합니다.") Double radiusKm) {
        Long userId = UserContext.getUserId();
        return ApiResult.success(new RoomListResponse(chatRoomService.searchOpenGroupRooms(keyword, userId, latitude, longitude, radiusKm)));
    }

    @GetMapping("/open-rooms/joined")
    @AuthRequired
    public ApiResult<RoomListResponse> getJoinedOpenGroupRooms() {
        Long userId = UserContext.getUserId();
        return ApiResult.success(new RoomListResponse(chatRoomService.getJoinedOpenGroupRooms(userId)));
    }

    @PostMapping("/open-rooms/{roomId}/join")
    @AuthRequired
    public ApiResult<RoomInfoResponse> joinOpenGroupRoom(
        @PathVariable @Positive(message = "roomId는 1 이상이어야 합니다.") Long roomId) {
        Long userId = UserContext.getUserId();
        return ApiResult.success(chatRoomService.joinOpenGroupRoom(roomId, userId));
    }

    @PostMapping("/open-rooms/{roomId}/leave")
    @AuthRequired
    public ApiResult<String> leaveOpenGroupRoom(
        @PathVariable @Positive(message = "roomId는 1 이상이어야 합니다.") Long roomId) {
        Long userId = UserContext.getUserId();
        chatRoomService.leaveOpenGroupRoom(roomId, userId);
        return ApiResult.success("leave success");
    }

    @GetMapping("/rooms")
    @AuthRequired
    public ApiResult<RoomListResponse> getUserChatRooms() {
        Long userId = UserContext.getUserId();
        User user = userService.getUserById(userId);

        return ApiResult.success(new RoomListResponse(chatRoomService.getUserChatRooms(user)));
    }

    @GetMapping("/rooms/{roomId}")
    @AuthRequired
    public ApiResult<RoomInfoResponse> getRoomInfo(
        @PathVariable @Positive(message = "roomId는 1 이상이어야 합니다.") Long roomId) {
        Long userId = UserContext.getUserId();
        return ApiResult.success(chatRoomService.getRoomInfo(roomId, userId));
    }

    @GetMapping("/history")
    @AuthRequired
    public ApiResult<ChatHistoryPageResponse> getChatHistory(
        @RequestParam(name = "room_id") @Positive(message = "room_id는 1 이상이어야 합니다.") Long roomId,
        @RequestParam(name = "before_message_seq", required = false) @Positive(message = "before_message_seq는 1 이상이어야 합니다.") Long beforeMessageSeq,
        @RequestParam(defaultValue = "30") @Positive(message = "size는 1 이상이어야 합니다.") @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        Long userId = UserContext.getUserId();
        return ApiResult.success(chatRoomService.getChatHistory(roomId, userId, beforeMessageSeq, size));
    }

    @PostMapping("/rooms/{roomId}/read")
    @AuthRequired
    public ApiResult<String> markRoomAsRead(
        @PathVariable @Positive(message = "roomId는 1 이상이어야 합니다.") Long roomId,
        @RequestParam(name = "read_upto_seq", required = false) @Positive(message = "read_upto_seq는 1 이상이어야 합니다.") Long readUptoSeq) {
        Long userId = UserContext.getUserId();
        chatRoomService.markRoomAsRead(roomId, userId, readUptoSeq);
        return ApiResult.success("read success");
    }


    
}
