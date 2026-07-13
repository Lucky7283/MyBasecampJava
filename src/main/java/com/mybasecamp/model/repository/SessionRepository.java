package com.mybasecamp.model.repository;

import com.mybasecamp.model.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    Optional<Session> findByToken(String token);
    List<Session> findByUserId(Integer userId);
    void deleteByToken(String token);
    void deleteByUserId(Integer userId);
    void deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}