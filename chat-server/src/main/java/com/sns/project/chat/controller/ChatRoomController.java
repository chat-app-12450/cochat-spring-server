package com.sns.project.chat.controller;

import com.sns.project.aspect.AuthRequired;
import com.sns.project.aspect.UserContext;
import com.sns.project.chat.controller.dto.request.RoomCreationRequest;
import com.sns.project.chat.controller.dto.response.RoomListResponse;
import com.sns.project.chat.controller.dto.response.RoomInfoResponse;
import com.sns.project.core.domain.user.User;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.service.user.UserService;

import lombok.RequiredArgsConstructor;

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




    
}
