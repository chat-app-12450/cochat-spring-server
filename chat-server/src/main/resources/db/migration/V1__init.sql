CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(255),
    user_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification_content (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    description VARCHAR(2000) NOT NULL,
    latitude DOUBLE,
    location_label VARCHAR(120),
    longitude DOUBLE,
    price BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL,
    reserved_buyer_id BIGINT,
    seller_id BIGINT NOT NULL,
    sold_buyer_id BIGINT,
    PRIMARY KEY (id),
    KEY idx_products_status_created_at (status, created_at),
    KEY idx_products_seller_created_at (seller_id, created_at),
    KEY idx_products_lat_lng (latitude, longitude),
    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users (id),
    CONSTRAINT fk_products_reserved_buyer FOREIGN KEY (reserved_buyer_id) REFERENCES users (id),
    CONSTRAINT fk_products_sold_buyer FOREIGN KEY (sold_buyer_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_room (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chat_room_type VARCHAR(255),
    description VARCHAR(500),
    last_message_seq BIGINT NOT NULL,
    latest_message_at DATETIME(6),
    latest_message_id BIGINT,
    latitude DOUBLE,
    location_label VARCHAR(120),
    longitude DOUBLE,
    max_participants INT,
    name VARCHAR(255) NOT NULL,
    open_chat BOOLEAN NOT NULL,
    creator_id BIGINT NOT NULL,
    product_id BIGINT,
    PRIMARY KEY (id),
    KEY idx_chat_room_latest_message_at (latest_message_at),
    KEY idx_chat_room_type (chat_room_type),
    KEY idx_chat_room_open_chat (open_chat),
    KEY idx_chat_room_last_message_seq (last_message_seq),
    KEY idx_chat_room_lat_lng (latitude, longitude),
    CONSTRAINT fk_chat_room_creator FOREIGN KEY (creator_id) REFERENCES users (id),
    CONSTRAINT fk_chat_room_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_bot_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content TEXT NOT NULL,
    `timestamp` DATETIME(6) NOT NULL,
    type VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    chat_room_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_bot_message_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message VARCHAR(255) NOT NULL,
    message_seq BIGINT NOT NULL,
    received_at DATETIME(6) NOT NULL,
    unread_count BIGINT NOT NULL DEFAULT 0,
    chat_room_id BIGINT,
    sender_id BIGINT,
    PRIMARY KEY (id),
    KEY idx_chat_message_room_id_id (chat_room_id, id),
    KEY idx_chat_message_room_id_message_seq (chat_room_id, message_seq),
    CONSTRAINT fk_chat_message_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_participant (
    id BIGINT NOT NULL AUTO_INCREMENT,
    join_seq BIGINT NOT NULL,
    last_read_seq BIGINT NOT NULL,
    leave_seq BIGINT,
    chat_room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_chat_participant_room_user_leave_seq (chat_room_id, user_id, leave_seq),
    KEY idx_chat_participant_room_leave_seq (chat_room_id, leave_seq),
    CONSTRAINT fk_chat_participant_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_chat_participant_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE chat_read_status (
    id BIGINT NOT NULL AUTO_INCREMENT,
    last_read_seq BIGINT NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    chat_room_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_read_status_user_room (user_id, chat_room_id),
    KEY idx_chat_read_status_user_room (user_id, chat_room_id),
    CONSTRAINT fk_chat_read_status_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_chat_read_status_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE follow (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    following_id BIGINT NOT NULL,
    follower_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_follow_follower_following (follower_id, following_id),
    CONSTRAINT fk_follow_following FOREIGN KEY (following_id) REFERENCES users (id),
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6),
    notification_content_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_content FOREIGN KEY (notification_content_id) REFERENCES notification_content (id),
    CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE outbox_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aggregate_id BIGINT NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    event_key VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    published_at DATETIME(6),
    retry_count INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    topic VARCHAR(200) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_outbox_event_status_id (status, id),
    KEY idx_outbox_event_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    image_url VARCHAR(2048) NOT NULL,
    sort_order INT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_verified_location (
    id BIGINT NOT NULL AUTO_INCREMENT,
    latitude DOUBLE NOT NULL,
    location_label VARCHAR(120),
    longitude DOUBLE NOT NULL,
    verified_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_verified_location_user (user_id),
    KEY idx_user_verified_location_verified_at (verified_at),
    CONSTRAINT fk_user_verified_location_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
