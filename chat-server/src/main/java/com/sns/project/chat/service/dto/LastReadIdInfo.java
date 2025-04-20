package com.sns.project.chat.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LastReadIdInfo {
    private Long prevLastReadId;
    private Long newLastReadId;
}
