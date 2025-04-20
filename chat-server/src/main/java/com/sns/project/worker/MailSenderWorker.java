package com.sns.project.worker;


import com.sns.project.config.constants.RedisKeys.PasswordReset;
import io.lettuce.core.RedisConnectionException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.sns.project.handler.exceptionHandler.exception.EmailNotSentException;
import com.sns.project.service.RedisService;


@Component
@RequiredArgsConstructor
@Slf4j
public class MailSenderWorker {

    private final RedisService redisService;
    private final JavaMailSender mailSender;

    public void processTasks() {
        while (true) {
            try {
                String key = PasswordReset.MAIL_QUEUE.get();
                MailTask task = redisService.popFromQueueBlocking(key, MailTask.class, 10);
                if (task != null) {
                    sendEmail(task);
                }
            } catch (QueryTimeoutException | RedisConnectionException e) {
//                log.error("Redis 연결 오류: {}", e.getMessage());
                try {
//                    log.info("Redis 재연결 시도 중...");
                    Thread.sleep(10000); // 10초 대기
                    continue; // while 루프 계속 실행
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
//                    log.warn("Redis 재연결 대기 중 인터럽트 발생");
                    continue; // 인터럽트 발생해도 계속 실행
                }
            } catch (Exception e) {
                log.error("메일 작업 처리 중 오류 발생", e);
                try {
                    Thread.sleep(1000);
                    continue; // while 루프 계속 실행
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("오류 복구 대기 중 인터럽트 발생");
                    continue; // 인터럽트 발생해도 계속 실행
                }
            }
        }
    }



    private void sendEmail(MailTask task) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(task.getEmail());
            helper.setSubject(task.getSubject());
            helper.setText(task.getContent(), true);

            mailSender.send(mimeMessage);
            log.info("메일 전송 성공 ㅠㅠ {}", task.getEmail());
        } catch (MessagingException | MailException e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            e.printStackTrace();
            throw new EmailNotSentException();
        }
    }
}
