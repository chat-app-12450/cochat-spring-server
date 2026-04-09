ALTER TABLE public.chat_message
    ADD COLUMN unread_count bigint NOT NULL DEFAULT 0;

UPDATE public.chat_message cm
SET unread_count = projected.unread_count
FROM (
    SELECT
        cm2.id,
        COUNT(cp.id)::bigint AS unread_count
    FROM public.chat_message cm2
    JOIN public.chat_participant cp
        ON cp.chat_room_id = cm2.chat_room_id
    WHERE cp.user_id <> cm2.sender_id
      AND cp.join_seq <= cm2.message_seq
      AND (cp.leave_seq IS NULL OR cm2.message_seq < cp.leave_seq)
      AND cp.last_read_seq < cm2.message_seq
    GROUP BY cm2.id
) projected
WHERE cm.id = projected.id;

ALTER TABLE public.chat_message
    ALTER COLUMN unread_count DROP DEFAULT;
