package com.sns.project.chat.controller;

import com.sns.project.env.aspect.AuthRequired;
import com.sns.project.env.aspect.UserContext;
import com.sns.project.chat.controller.dto.request.RoomCreationRequest;
import com.sns.project.chat.controller.dto.response.RoomListResponse;
import com.sns.project.chat.controller.dto.response.ChatHistoryResponse;
import com.sns.project.chat.controller.dto.response.RoomInfoResponse;
import com.sns.project.core.domain.user.User;
import com.sns.project.env.handler.response.ApiResult;
import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.user.service.UserService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")  // Allow requests from frontend

public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final UserService userService;

    @PostMapping("/room")
    @AuthRequired
    public ApiResult<RoomInfoResponse> createRoom(@RequestBody RoomCreationRequest roomCreationRequest) {
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
//    @AuthRequired
    public ApiResult<List<ChatHistoryResponse>> getChatHistory(
        @RequestParam(name = "room_id") Long roomId) {
        System.out.println("🤔 채팅내역");
        return ApiResult.success(chatRoomService.getChatHistory(roomId));
    }


    
}
