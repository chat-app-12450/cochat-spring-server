
package com.sns.project.controller.noticiation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestNotificationDto {
    @NotNull
    private Long userId;
    @NotNull
    private String message;
}
