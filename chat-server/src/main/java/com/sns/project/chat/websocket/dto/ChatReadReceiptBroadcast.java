package com.sns.project.chat.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatReadReceiptBroadcast {
    private final String type = "READ";
    private Long roomId;
    private Long readerId;
    private Long previousReadSeq;
    private Long newReadSeq;
}
