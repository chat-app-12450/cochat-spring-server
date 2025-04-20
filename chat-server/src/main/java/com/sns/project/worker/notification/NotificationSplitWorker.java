package com.sns.project.worker.notification;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sns.project.controller.noticiation.dto.workerDto.RawNotificationDto;
import com.sns.project.controller.noticiation.dto.workerDto.BatchProcessedNotificationDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSplitWorker  {

    private final RedisNotificationWorker redisNotificationWorker;
    private final BlockingQueue<RawNotificationDto> rawNotificationQueue = new LinkedBlockingQueue<>();
    private static final int BATCH_SIZE = 100;
    @Transactional
    public void acceptTask() {
        while (true) {
            try {
                // ✅ 큐에서 알림 가져와서 배치로 저장
                RawNotificationDto rawNotification = rawNotificationQueue.take();
                work(rawNotification);
            } catch (Exception e) {
                log.error("알림 분할 작업 처리 중 예외 발생", e);
            }
        }
    }


    public void work(RawNotificationDto rawNotification) {
        Long contentId = rawNotification.getContentId(); 
        List<Long> recipientIds = rawNotification.getRecipientIds();
    
        int totalBatches = (int) Math.ceil((double) recipientIds.size() / BATCH_SIZE);
        for (int i = 0; i < totalBatches; i++) {
            List<Long> batchRecipients = recipientIds.subList(i * BATCH_SIZE, Math.min((i + 1) * BATCH_SIZE, recipientIds.size()));
    
            BatchProcessedNotificationDto batchDto = BatchProcessedNotificationDto.builder()
                    .senderId(rawNotification.getSenderId())
                    .contentId(contentId) 
                    .recipientIds(batchRecipients)
                    .build();
    
            redisNotificationWorker.enqueue(batchDto);
        }
    }
        

    public void enqueue(RawNotificationDto  rawNotificationDto) {
        try {
            rawNotificationQueue.put(rawNotificationDto);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
