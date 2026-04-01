package com.sns.project.chat.controller.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreationRequest {
    @Size(max = 100, message = "name은 100자 이하여야 합니다.")
    private String name;
    @NotEmpty(message = "최소 한 명 이상의 초대 대상이 필요합니다.")
    @Size(max = 50, message = "한 번에 초대할 수 있는 사용자는 50명 이하입니다.")
    private List<@NotNull(message = "userIds에는 null이 올 수 없습니다.") @Positive(message = "userIds는 1 이상이어야 합니다.") Long> userIds;
}
