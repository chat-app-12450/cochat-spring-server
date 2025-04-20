package com.sns.project.controller.noticiation.dto.workerDto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BatchProcessedNotificationDto {
    private Long senderId;
    private List<Long> recipientIds;
    private Long contentId;
}
