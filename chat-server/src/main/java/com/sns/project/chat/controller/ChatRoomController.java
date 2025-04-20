package com.sns.project.chat.controller;

import com.sns.project.aspect.AuthRequired;
import com.sns.project.aspect.UserContext;
import com.sns.project.domain.user.User;
import com.sns.project.chat.dto.request.ChatRoomRequest;
import com.sns.project.chat.dto.response.AllChatRoomResponse;
import com.sns.project.chat.dto.response.ChatRoomResponse;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.chat.service.ChatService;
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
    private final ChatService chatService;

    @PostMapping("/room")
    @AuthRequired
    public ApiResult<ChatRoomResponse> createRoom(@RequestBody ChatRoomRequest chatRoomRequest) {
        Long userId = UserContext.getUserId();
        User creator = userService.getUserById(userId);

        return ApiResult.success(chatRoomService.createRoom(
            chatRoomRequest.getName(),
            chatRoomRequest.getUserIds(),
            creator
        ));
    }

    @GetMapping("/rooms")
    @AuthRequired
    public ApiResult<AllChatRoomResponse> getUserChatRooms() {
        Long userId = UserContext.getUserId();
        User user = userService.getUserById(userId);

        return ApiResult.success(new AllChatRoomResponse(chatRoomService.getUserChatRooms(user)));
    }




    
}
