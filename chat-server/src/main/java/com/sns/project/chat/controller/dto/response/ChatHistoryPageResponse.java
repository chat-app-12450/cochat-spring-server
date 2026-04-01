package com.sns.project.chat.controller.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatHistoryPageResponse {

    private final List<ChatHistoryResponse> messages;
    private final Long nextBeforeMessageId;
    private final boolean hasMore;
}
