package com.sns.project.controller.post.post.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPostDto {
    @NotNull
    private String title;
    @NotNull
    private String content;
}