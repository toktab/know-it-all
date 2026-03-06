package com.knowitall.game.repository;

import com.knowitall.game.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    List<GameSession> findByUserIdOrderByStartedAtDesc(Long userId);
    Optional<GameSession> findByIdAndUserId(Long id, Long userId);
}