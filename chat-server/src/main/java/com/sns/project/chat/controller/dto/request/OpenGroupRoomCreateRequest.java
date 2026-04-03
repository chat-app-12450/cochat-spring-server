package com.sns.project.chat.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenGroupRoomCreateRequest {

    @NotBlank(message = "name은 비어 있을 수 없습니다.")
    @Size(max = 100, message = "name은 100자 이하여야 합니다.")
    private String name;

    @Size(max = 500, message = "description은 500자 이하여야 합니다.")
    private String description;

    @NotNull(message = "maxParticipants는 필수입니다.")
    @Min(value = 2, message = "maxParticipants는 2 이상이어야 합니다.")
    @Max(value = 500, message = "maxParticipants는 500 이하여야 합니다.")
    private Integer maxParticipants;
}
