package com.sns.project.chat.controller.dto.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatHistoryPageResponse {

    private final List<ChatHistoryResponse> messages;
    private final Long nextBeforeMessageSeq;
    private final boolean hasMore;
    private final Map<Long, Long> readSeqSnapshot;
}
