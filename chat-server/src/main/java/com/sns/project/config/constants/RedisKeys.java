package com.sns.project.config.constants;

public class RedisKeys {

    public enum Chat {
        CHAT_MESSAGE_BATCH_SET_KEY("chat:message:batch:"),

        CHAT_ROOM_PARTICIPANTS_SET_KEY("chat:room:users:"), // 채팅방에 속한 유저목록
        CHAT_UNREAD_COUNT_HASH_KEY("chat:unread:count:"), // 메시지 별 안읽음 수
        CONNECTED_USERS_SET_KEY("chat:presence:"), // 채팅방에 접속한 유저목록
        // CHAT_READ_USERS_SET_KEY("chat:read:users:"), // 메시지 별 읽음 유저목록
        CHAT_LAST_READ_MESSAGE_ID("chat:last:read:message:"), // 유저 별 마지막 읽음 메시지 아이디
        CHAT_MESSAGES_KEY("chat:messages:"), // 채팅방 메시지
        CHAT_LOCK_KEY("chat:lock:"); // 채팅방 락
        // CHAT_ROOM_INFO_KEY("chat:room:info:"); // 채팅방 정보

        private final String key;
        Chat(String key) { this.key = key; }
//        public String get() { return key; }

        public String getLastReadMessageKey(Long userId, Long roomId) {
            return this.key + userId + ":" + roomId;
        }
        public String getUnreadCountKey() {
            return this.key;
        }
        
        public String getConnectedKey(Long roomId) {
            return this.key + roomId;
        }
        
        public String getMessagesKey(Long roomId) {
            return this.key + roomId;
        }

        public String getReadUserKey(String messageId) {
            return this.key + messageId;
        }
        public String getReadUserKeyPrefix() {
            return this.key;
        }
        public String getMessageBatchQueueKey() {
            return this.key;
        }

        public String getParticipants(Long id) {
            return this.key + id;
        }

        public String getLockKey(Long userId, Long roomId) {
            return this.key + userId + ":" + roomId;
        }

        public String getPrefix() {
            return this.key;
        }

        public String getLastReadMessageKeyPattern() {
            return this.key + "{userId}:{roomId}";
        }
    }
    
    public enum Auth {
        CACHE_PREFIX("auth:");
        
        private final String key;
        Auth(String key) { this.key = key; }
        public String get() { return key; }
    }

    public enum PasswordReset {
        MAIL_QUEUE("mail:queue"),
        RESET_TOKEN("password-reset:token:");

        private final String key;
        PasswordReset(String key) { this.key = key; }
        public String get() { return key; }
    }

    public enum User {
        CACHE_KEY("user:cache");

        private final String key;
        User(String key) { this.key = key; }
        public String get() { return key; }
    }

    public enum Notification {
        QUEUE_KEY("notification:queue");

        private final String key;
        Notification(String key) { this.key = key; }
        public String get() { return key; }
    }
}
