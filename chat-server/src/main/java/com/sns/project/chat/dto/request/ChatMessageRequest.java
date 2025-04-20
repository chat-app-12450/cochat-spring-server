package com.sns.project.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChatMessageRequest {
    @NotBlank(message = "Message cannot be empty")
    private String message;

    @NotBlank(message = "roomId cant be empty")
    private Long roomId;
}
