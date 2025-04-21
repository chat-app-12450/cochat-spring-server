package com.sns.project.chat_consumer.config;

import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.core.repository.user.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile({"dev", "default"}) // 개발 환경에서만 활성화
public class TestDataInitializer {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        initializeData();
    }

    /**
     * 트랜잭션 내에서 데이터를 초기화하기 위한 별도 메서드
     * @PostConstruct와 @Transactional은 함께 동작하지 않음
     */
    @Transactional
    public void initializeData() {
        // 테스트 사용자가 없으면 생성
        if (userRepository.count() == 0) {
            log.info("🔥 테스트 사용자 생성 중...");
            User user1 = User.builder()
                .id(1L)
                .email("user1@example.com")
                .userId("user1")
                .password("password")
                .name("User One")
                .build();
            
            User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .userId("user2")
                .password("password")
                .name("User Two")
                .build();
            
            userRepository.save(user1);
            userRepository.save(user2);
            log.info("✅ 테스트 사용자 생성 완료");
        }

        // 테스트 채팅방이 없으면 생성
        if (chatRoomRepository.count() == 0) {
            log.info("🔥 테스트 채팅방 생성 중...");
            ChatRoom chatRoom1 = ChatRoom.builder()
                .id(1L)
                .name("테스트 채팅방 1")
                .build();
            
            ChatRoom chatRoom2 = ChatRoom.builder()
                .id(2L)
                .name("테스트 채팅방 2")
                .build();
            
            chatRoomRepository.save(chatRoom1);
            chatRoomRepository.save(chatRoom2);
            log.info("✅ 테스트 채팅방 생성 완료");
        }
    }
} 