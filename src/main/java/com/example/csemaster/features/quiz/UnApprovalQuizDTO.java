package com.example.csemaster.features.quiz;

import com.example.csemaster.entity.QuizEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class UnApprovalQuizDTO {
    private Long quizId;
    private String subject;
    private String detailSubject;
    private String jsonContent;
    private LocalDateTime createAt;
    private String userNickname;

    public UnApprovalQuizDTO(QuizEntity quiz, String nickname) {
        this.quizId = quiz.getQuizId();
        this.subject = quiz.getSubject();
        this.detailSubject = quiz.getDetailSubject();
        this.jsonContent = quiz.getJsonContent();
        this.createAt = quiz.getCreateAt();
        this.userNickname = nickname;
    }
}
