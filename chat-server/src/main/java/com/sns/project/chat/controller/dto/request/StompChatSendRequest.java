package com.sns.project.chat.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StompChatSendRequest {

    // roomId는 STOMP destination 경로에서 읽고, body에는 사용자 입력 payload만 둔다.
    @NotBlank(message = "message는 비어 있을 수 없습니다.")
    @Size(max = 1000, message = "message는 1000자 이하여야 합니다.")
    private String message;

    @Size(max = 100, message = "clientMessageId는 100자 이하여야 합니다.")
    private String clientMessageId;
}
