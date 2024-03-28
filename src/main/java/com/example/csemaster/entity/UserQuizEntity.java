package com.example.csemaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_quiz")
public class UserQuizEntity {
    @Id
    @Column(name = "user_quiz_id")
    private Long userQuizId;

    @OneToOne
    @JoinColumn(name = "user_quiz_id", referencedColumnName = "quiz_id")
    private QuizEntity quiz;

    @Column(name = "permission_status")
    private Integer permissionStatus;

    @ManyToOne
    @JoinColumn(name = "user_id_for_user_quiz")
    private UserEntity userId;

    /*@ManyToOne
    @JoinColumn(name = "quiz_id")
    private QuizEntity quizzes;*/
}
