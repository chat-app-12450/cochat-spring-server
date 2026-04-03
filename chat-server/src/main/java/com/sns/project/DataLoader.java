package com.sns.project;

import com.sns.project.chat.controller.dto.response.RoomInfoResponse;
import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.core.repository.user.UserRepository;
import com.sns.project.follow.FollowingService;
import com.sns.project.user.UserService;
import com.sns.project.user.dto.request.RequestRegisterDto;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final FollowingService followingService;
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public void run(String... args) {
        initializeUsers(10);
        follow();
        seedOpenChatRooms();
    }

    private void initializeUsers(int userCount) {
        List<String> emails = new ArrayList<>();
        for (int i = 1; i <= userCount; i++) {
            emails.add(i + "@gmail.com");
        }
        emails.forEach(this::saveUserIfMissing);
    }

    private void saveUserIfMissing(String email) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        try {
            userService.register(createRegisterDto(email));
        } catch (Exception e) {
            log.warn("dev user seed skipped: {}", email, e);
        }
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

    private void follow() {
        try {
            followingService.followUser(1L, 2L);
            followingService.followUser(1L, 3L);
            followingService.followUser(1L, 4L);
            followingService.followUser(2L, 1L);
            followingService.followUser(3L, 1L);
            followingService.followUser(4L, 1L);
        } catch (IllegalArgumentException e) {
            log.info("dev follow seed skipped: {}", e.getMessage());
        }
    }

    private void seedOpenChatRooms() {
        Long neighborhoodRoomId = getOrCreateOpenChatRoom(
            "성수 직거래 오픈채팅",
            "동네 직거래 테스트용 공개 채팅방",
            50,
            1L
        );
        joinIfNeeded(neighborhoodRoomId, 2L, 3L, 4L);

        Long campusRoomId = getOrCreateOpenChatRoom(
            "대학생 중고거래 오픈채팅",
            "캠퍼스 근처 거래 테스트용 공개 채팅방",
            30,
            2L
        );
        joinIfNeeded(campusRoomId, 1L, 5L, 6L);
    }

    private Long getOrCreateOpenChatRoom(String name, String description, int maxParticipants, Long creatorId) {
        return chatRoomRepository.findFirstByNameAndOpenChatTrueOrderByIdAsc(name)
            .map(ChatRoom::getId)
            .orElseGet(() -> {
                User creator = userService.getUserById(creatorId);
                RoomInfoResponse room = chatRoomService.createOpenGroupRoom(name, description, maxParticipants, creator);
                return room.getId();
            });
    }

    private void joinIfNeeded(Long roomId, Long... userIds) {
        for (Long userId : userIds) {
            chatRoomService.joinOpenGroupRoom(roomId, userId);
        }
    }
}
