package com.sns.project.service.chat;

import com.sns.project.chat.service.ChatRedisService;
import com.sns.project.config.TestRedisConfig;
import com.sns.project.chat.service.ChatPresenceService;
import com.sns.project.chat.service.ChatReadService;
import com.sns.project.chat.service.ChatRoomService;
import com.sns.project.chat.service.ChatService;
import com.sns.project.config.constants.RedisKeys.Chat;
import com.sns.project.domain.chat.ChatParticipant;
import com.sns.project.repository.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.sns.project.DataLoader;
import com.sns.project.config.constants.RedisKeys;
import com.sns.project.chat.dto.response.WebsocketBroadcastResponse;
import com.sns.project.controller.user.dto.request.RequestRegisterDto;
import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.user.User;
import com.sns.project.repository.chat.ChatParticipantRepository;
import com.sns.project.repository.chat.ChatRoomRepository;
import com.sns.project.chat.service.RedisLuaService;
import com.sns.project.service.user.UserService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@Import(TestRedisConfig.class)  // ✅ Redis 설정 강제 적
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // ✅ 추가
class ChatServiceIntegrationTest {

    @Autowired
    private Environment environment;


    @Test
    void checkActiveProfiles() {
        System.out.println(">>> Active Profiles: " + Arrays.toString(environment.getActiveProfiles()));
        // ...
    }

    @Autowired
    private Environment env;

    @Autowired
    private LettuceConnectionFactory redisConnectionFactory;

    @Test
    void checkRedisConnection() {
        String redisHost = env.getProperty("spring.redis.host");
        String redisPort = env.getProperty("spring.redis.port");

        System.out.println("✅ 현재 Redis 설정: " + redisHost + ":" + redisPort);

        System.out.println("✅ Lettuce Redis 설정 확인:");
        System.out.println("  - Redis Host: " + redisConnectionFactory.getHostName());
        System.out.println("  - Redis Port: " + redisConnectionFactory.getPort());

    }


    @MockBean
    private DataLoader dataLoader;

    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatReadService chatReadService;
    @Autowired
    private ChatRedisService stringRedisService;
    @Autowired
    private ChatPresenceService chatPresenceService;
    @Autowired
    private RedisLuaService redisLuaService;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private ChatParticipantRepository chatParticipantRepository;
    @Autowired
    private UserRepository userRepository;


    /*
     * 채팅방 참여자: 4명
     */
    void setUp() {
        System.out.println("✅ 현재 활성화된 프로파일: " + System.getProperty("spring.profiles.active"));

        // 테스트용 사용자 생성

        User sender = User.builder()
            .userId("sender")
            .name("보내는사람")
            .email("sender@test.com")
            .password("password")
            .build();
        
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = User.builder()
                .userId("user" + i)
                .name("사용자" + i)
                .email("user" + i + "@test.com")
                .password("password")
                .build();
            userList.add(user);
        }

        User savedSender = userService.register(RequestRegisterDto.fromEntity(sender));
        List<User> savedUserList = new ArrayList<>();
        for (User user : userList) {
            savedUserList.add(userService.register(RequestRegisterDto.fromEntity(user)));
        }

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
            .name("테스트방")
            .creator(savedSender)
            .build();
        chatRoom = chatRoomRepository.save(chatRoom);

        // 채팅방 참여자 설정
        chatParticipantRepository.save(new ChatParticipant(chatRoom, savedSender));
        for (User user : savedUserList) {
            chatParticipantRepository.save(new ChatParticipant(chatRoom, user));
        }

        // Redis 초기 데이터 설정
        String participantsKey = RedisKeys.Chat.CHAT_ROOM_PARTICIPANTS_SET_KEY
            .getParticipants(chatRoom.getId());
        stringRedisService.addToSet(participantsKey, savedSender.getId().toString());
        for (User user : savedUserList) {
            stringRedisService.addToSet(participantsKey, user.getId().toString());
        }
    }

    @Test
    void 동시에_여러_읽음처리_요청_실제_Redis_테스트() throws InterruptedException {
        setUp();

        // given
        User sender = userService.getUserById(1L);
        ChatRoom chatRoom = chatRoomRepository.findAll().get(0);

        // 메시지 전송
        WebsocketBroadcastResponse savedMessage = chatService.saveMessage(
            sender.getId(), 
            "test message", 
            chatRoom.getId()
        );

        // when
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        System.out.println("참여 멤버들: ");
        System.out.println(stringRedisService.getSetMembers(Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(chatRoom.getId())));
        System.out.println(stringRedisService.getHashValue(
            RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey(),
            savedMessage.getId().toString()
        ).orElse("0"));
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    chatReadService.markAllAsRead(4L, chatRoom.getId());
                    chatReadService.markAllAsRead(3L, chatRoom.getId());
                    chatReadService.markAllAsRead(2L, chatRoom.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
        String count = stringRedisService.getHashValue(
            unreadCountKey, 
            savedMessage.getId().toString()
        ).orElse("0");
        
        assertThat(count).isEqualTo("0");  // 모든 읽음 처리 후에는 0이어야 함
        
        for(long i=1; i<5; i++) {
            String lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID
                .getLastReadMessageKey(i, chatRoom.getId());
            String lastReadId = stringRedisService.getValue(lastReadKey).orElse("-1");
            assertThat(lastReadId).isEqualTo(savedMessage.getId().toString());
        }

    }

    @Test
    void 메시지_저장시_접속자는_자동_읽음처리() {
        setUp();

        // given
        User sender = userService.getUserById(1L);
        User receiver = userService.getUserById(2L);
        ChatRoom chatRoom = chatRoomRepository.findAll().get(0);

        // 수신자를 접속 상태로 설정
        String connectedUsersKey = Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(chatRoom.getId());
        stringRedisService.addToSet(connectedUsersKey, receiver.getId().toString());

        // when
        WebsocketBroadcastResponse savedMessage = chatService.saveMessage(
            sender.getId(), 
            "test message", 
            chatRoom.getId()
        );

        // then
        // 1. 안읽음 수가 0인지 확인
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
        String count = stringRedisService.getHashValue(
            unreadCountKey, 
            savedMessage.getId().toString()
        ).orElse("-1");
        assertThat(count).isEqualTo("2");

//        System.out.println("g현재 까지 보냔 메시즐");
//        System.out.println(stringRedisService.getZSetRange(
//            Chat.CHAT_MESSAGES_KEY.getMessagesKey(chatRoom.getId()),
//            0, -1));

        // 2. 수신자의 last_read_id가 현재 메시지 ID로 설정되었는지 확인
        String lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID
            .getLastReadMessageKey(receiver.getId(), chatRoom.getId());
        String lastReadId = stringRedisService.getValue(lastReadKey).orElse("-1");
        assertThat(lastReadId).isEqualTo(savedMessage.getId().toString());

        // 3. 송신자의 last_read_id가 현재 메시지 ID로 설정되었는지 확인
        lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID
            .getLastReadMessageKey(sender.getId(), chatRoom.getId());
        lastReadId = stringRedisService.getValue(lastReadKey).orElse("-1");
            assertThat(lastReadId).isEqualTo(savedMessage.getId().toString());
    }

    @Test
    void 새메시지처리가_입장처리보다_빠르면() throws InterruptedException {
        setUp();

        // given
        User sender = userService.getUserById(1L);
        User receiver = userService.getUserById(2L);
        ChatRoom chatRoom = chatRoomRepository.findAll().get(0);

        // Simulate sending a message and marking it as read
        for(int i=0; i<2; i++) {
            chatService.saveMessage(
                sender.getId(), 
                "message" + i, 
                chatRoom.getId()
            );
        }

        chatPresenceService.userEnteredRoom(receiver.getId(), chatRoom.getId());
        stringRedisService.addToSet(
            Chat.CONNECTED_USERS_SET_KEY.getConnectedKey(chatRoom.getId()),
            receiver.getId().toString()
        );

        // Simulate a new message arriving
        WebsocketBroadcastResponse lastSentMessage = chatService.saveMessage(
            sender.getId(), 
            "new message", 
            chatRoom.getId()
        );

        // Simulate marking messages as read
        chatReadService.markAllAsRead(receiver.getId(), chatRoom.getId());


        // then
        // Check that the unread count for the new message is correct
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();



        for(long i=1; i<=3; i++) {
            String count = stringRedisService.getHashValue(
                unreadCountKey,
                String.valueOf(i)
            ).orElse("-1");
            assertThat(count).isEqualTo("2");
        }

        // Check that the last read ID is updated correctly
        String lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID
            .getLastReadMessageKey(receiver.getId(), chatRoom.getId());
        String lastReadId = stringRedisService.getValue(lastReadKey).orElse("-1");
        assertThat(lastReadId).isEqualTo(lastSentMessage.getId().toString());
    }


    @BeforeEach
    void tearDown() {
        // 테스트 후 테스트 데이터 정리
        stringRedisService.deletePattern("*");
    }

} 