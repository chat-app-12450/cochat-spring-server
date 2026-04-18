ALTER TABLE chat_message
    ADD COLUMN IF NOT EXISTS unread_count BIGINT NOT NULL DEFAULT 0;

UPDATE chat_message cm
LEFT JOIN (
    SELECT
        cm2.id,
        COUNT(cp.id) AS unread_count
    FROM chat_message cm2
    JOIN chat_participant cp
        ON cp.chat_room_id = cm2.chat_room_id
    WHERE cp.user_id <> cm2.sender_id
      AND cp.join_seq <= cm2.message_seq
      AND (cp.leave_seq IS NULL OR cm2.message_seq < cp.leave_seq)
      AND cp.last_read_seq < cm2.message_seq
    GROUP BY cm2.id
) projected
    ON projected.id = cm.id
SET cm.unread_count = COALESCE(projected.unread_count, 0);
