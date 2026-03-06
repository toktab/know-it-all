package com.knowitall.game.repository;

import com.knowitall.game.entity.GameSession;
import com.knowitall.game.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findBySessionIdOrderByAskedAt(Long sessionId);
    List<QuizQuestion> findBySession(GameSession session);
}