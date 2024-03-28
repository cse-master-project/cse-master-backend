package com.example.csemaster.repository;

import com.example.csemaster.entity.ActiveQuizEntity;
import com.example.csemaster.entity.QuizEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<ActiveQuizEntity> findAllBy(Pageable pageable);

    @Query("SELECT q FROM ActiveQuizEntity q " +
            "WHERE q NOT IN (SELECT u.quiz FROM UserQuizEntity u)")
    Page<ActiveQuizEntity> findAllUserQuiz(Pageable pageable);

    @Query("SELECT q FROM ActiveQuizEntity q " +
            "WHERE q NOT IN (SELECT d.quiz FROM DefaultQuizEntity d)")
    Page<ActiveQuizEntity> findAllDefaultQuiz(Pageable pageable);
}
