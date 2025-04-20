package com.sns.project.controller.noticiation;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sns.project.aspect.AuthRequired;
import com.sns.project.aspect.UserContext;
import com.sns.project.domain.notification.Notification;
import com.sns.project.controller.noticiation.dto.response.ResponseNotificationListDto;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/all")
    @AuthRequired
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all notifications", description = "Retrieve a paginated list of notifications for the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ApiResult<ResponseNotificationListDto> getNotifications(
            @Parameter(description = "Page number for pagination", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size for pagination", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        Page<Notification> notifications = notificationService.getNotifications(userId, page, size);

        return ApiResult.success(new ResponseNotificationListDto(notifications.getContent(), page, size, notifications.getTotalPages()));
    }

    @DeleteMapping("/{notificationId}")
    @AuthRequired
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a notification", description = "Delete a notification by its ID for the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted notification"),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ApiResult<Long> deleteNotification(
            @Parameter(description = "ID of the notification to be deleted", required = true)
            @PathVariable Long notificationId) {
        Long userId = UserContext.getUserId();
        notificationService.deleteNotification(notificationId, userId);
        return ApiResult.success(notificationId);
    }
}
