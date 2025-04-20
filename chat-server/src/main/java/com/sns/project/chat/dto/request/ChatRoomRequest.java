package com.sns.project.chat.dto.request;

import java.util.List;
import java.util.Set;

import com.sns.project.domain.chat.ChatRoomType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomRequest {
    private String name;
    private List<Long> userIds;
}