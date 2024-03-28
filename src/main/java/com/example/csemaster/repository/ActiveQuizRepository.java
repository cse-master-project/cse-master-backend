package com.example.csemaster.repository;

import com.example.csemaster.entity.ActiveQuizEntity;
import com.example.csemaster.entity.QuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActiveQuizRepository extends JpaRepository<ActiveQuizEntity, Long> {
    @Query("SELECT q " +
            "FROM ActiveQuizEntity q " +
            "LEFT OUTER JOIN QuizLogEntity l ON q.quizId = l.quizId AND l.userId = :userId " +
            "WHERE q.subject = :subject AND q.detailSubject = :detailSubject")
    List<QuizEntity> getAnOpenQuiz(@Param("userId") String userId, @Param("subject") String subject, @Param("detailSubject") String detailSubject);

}
