package com.sns.project;

import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.core.domain.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sns.project.controller.user.dto.request.RequestRegisterDto;
import com.sns.project.service.NotificationService;
import com.sns.project.service.RedisService;
import com.sns.project.service.following.FollowingService;
import com.sns.project.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final UserService userService;
    private final RedisService redisService;
    private final NotificationService notificationService;
    private final FollowingService followingService;
    private final Random random = new Random();
    private final ChatRoomService chatRoomService;

        @Override
        public void run(String... args) {
            int userCount = 10;
            initializeUsers(userCount);
            initializeUserTokens(userCount);

            follow();
            saveChatRooms();
        }
    
        private void initializeUsers(int userCount) {
            List<String> emails = new ArrayList<>();
            for(int i=1; i<=userCount; i++){
                emails.add(i+"@gmail.com");
            }
            emails.forEach(this::saveUser);
        }
    
        private void initializeUserTokens(int userCount) {

            for (long i = 1; i <= userCount; i++) {
                saveUserToken(i, "testToken"+i);
            }
        }
    
        private void saveUserToken(Long userId, String token) {
            redisService.setValueWithExpiration(token, String.valueOf(userId), 10000 * 60);
        }
    
        private void saveUser(String email) {
            RequestRegisterDto requestRegisterDto = createRegisterDto(email);
            userService.register(requestRegisterDto);
        }
    
        private RequestRegisterDto createRegisterDto(String email) {
            RequestRegisterDto dto = new RequestRegisterDto();
            dto.setEmail(email);
            dto.setPassword("1234");
            dto.setName("test");
            dto.setUserId(generateUserId(email));
            return dto;
        }
    
        private String generateUserId(String email) {
            return email.split("@")[0] + "_id";
        }
    
//        private void saveNotifications() {
//            Long senderId = 3L;
//            List<Long> receiverIds = List.of(1L, 2L);
//            for (int i = 0; i < 3; i++) {
//                notificationService.sendNotification("test notification" + i, senderId, receiverIds);
//            }
//        }



    

    

    
        /*
         * 1번 유저의 팔로잉 : 2, 3, 4,
         * 2번 유저의 팔로잉 : 1,
         * 3번 유저의 팔로잉 : 1,
         * 4번 유저의 팔로잉 : 1,
         */
        private void follow() {
            // 1번 유저가 2, 3, 4번 유저를 팔로우
            followingService.followUser(1L, 2L);
            followingService.followUser(1L, 3L);
            followingService.followUser(1L, 4L);
    
            // 1번 유저 팔로워 2, 3 ,4 (3명)
            followingService.followUser(2L, 1L);
            followingService.followUser(3L, 1L);
            followingService.followUser(4L, 1L);
            
        }
    
        private void saveChatRooms() {
            User creator = userService.getUserById(1L);
            List<Long> parties = new ArrayList<>();
            for(long i=1; i<=100; i++){
                parties.add(i);
            }
            chatRoomService.createRoom("test", parties, creator);
            chatRoomService.createRoom("test2", parties, creator);
            chatRoomService.createRoom("test3", parties, creator);

//            chatService.saveMessage(1L, "test", 1L);
//            chatService.saveMessage(2L, "test", 1L);
//            chatService.saveMessage(3L, "test", 1L);
    }
}

