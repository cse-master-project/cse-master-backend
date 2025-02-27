package com.example.csemaster.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class QuizReportRequest {
    private Long quizId;
    @NotBlank(message = "Content cannot be empty.")
    private String content;
}
