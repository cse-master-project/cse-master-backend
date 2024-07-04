package com.example.csemaster.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuizReportResponse {
    private Long quizReportId;
    private Long quizId;
    private String userNickname;
    private String content;
    private LocalDateTime reportAt;
}
