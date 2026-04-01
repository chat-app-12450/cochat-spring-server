package com.sns.project.chat.controller;

import com.sns.project.auth.AuthRequired;
import com.sns.project.auth.UserContext;
import com.sns.project.chat.controller.dto.request.RoomCreationRequest;
import com.sns.project.chat.controller.dto.response.RoomListResponse;
import com.sns.project.chat.controller.dto.response.ChatHistoryResponse;
import com.sns.project.chat.controller.dto.response.ChatHistoryPageResponse;
import com.sns.project.chat.controller.dto.response.RoomInfoResponse;
import com.sns.project.core.domain.user.User;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
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

    @GetMapping("/rooms")
    @AuthRequired
    public ApiResult<RoomListResponse> getUserChatRooms() {
        Long userId = UserContext.getUserId();
        User user = userService.getUserById(userId);

        return ApiResult.success(new RoomListResponse(chatRoomService.getUserChatRooms(user)));
    }

    @GetMapping("/history")
    @AuthRequired
    public ApiResult<ChatHistoryPageResponse> getChatHistory(
        @RequestParam(name = "room_id") @Positive(message = "room_id는 1 이상이어야 합니다.") Long roomId,
        @RequestParam(name = "before_message_id", required = false) @Positive(message = "before_message_id는 1 이상이어야 합니다.") Long beforeMessageId,
        @RequestParam(defaultValue = "30") @Positive(message = "size는 1 이상이어야 합니다.") @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        Long userId = UserContext.getUserId();
        return ApiResult.success(chatRoomService.getChatHistory(roomId, userId, beforeMessageId, size));
    }


    
}
