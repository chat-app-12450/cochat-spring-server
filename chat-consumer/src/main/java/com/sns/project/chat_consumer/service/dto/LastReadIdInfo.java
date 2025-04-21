package com.sns.project.chat_consumer.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LastReadIdInfo {
    private Long prevLastReadId;
    private Long newLastReadId;
} 