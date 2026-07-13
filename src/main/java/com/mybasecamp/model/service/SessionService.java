package com.mybasecamp.model.service;

import com.mybasecamp.model.entity.Session;
import com.mybasecamp.model.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Создаёт новую сессию для пользователя.
     * Если у пользователя уже есть активные сессии — они не трогаются
     * (можно вызвать invalidateUserSessions заранее, если нужен single-session режим).
     */
    public Session createSession(Integer userId, String token, LocalDateTime expiresAt) {
        Session session = new Session();
        session.setUserId(userId);
        session.setToken(token);
        session.setExpiresAt(expiresAt);
        return sessionRepository.save(session);
    }

    /**
     * Создаёт новую сессию с автогенерацией токена.
     */
    public Session createSession(Integer userId, LocalDateTime expiresAt) {
        String token = generateToken();
        return createSession(userId, token, expiresAt);
    }

    /**
     * Создаёт новую сессию, предварительно завершив все текущие сессии пользователя.
     * Полезно, если нужно ограничить пользователя одной активной сессией.
     */
    public Session createSingleSession(Integer userId, LocalDateTime expiresAt) {
        invalidateUserSessions(userId);
        return createSession(userId, expiresAt);
    }

    public Optional<Session> getSessionByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    public List<Session> getSessionsByUserId(Integer userId) {
        return sessionRepository.findByUserId(userId);
    }

    /**
     * Проверяет, что сессия существует и ещё не истекла.
     */
    public boolean isSessionValid(String token) {
        return getSessionByToken(token)
                .map(session -> session.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    /**
     * Продлевает срок действия существующей сессии.
     */
    public Optional<Session> extendSession(String token, LocalDateTime newExpiresAt) {
        return getSessionByToken(token).map(session -> {
            session.setExpiresAt(newExpiresAt);
            return sessionRepository.save(session);
        });
    }

    public void deleteSession(String token) {
        sessionRepository.deleteByToken(token);
    }

    /**
     * Удаляет все сессии пользователя (например, при смене пароля или logout со всех устройств).
     */
    public void invalidateUserSessions(Integer userId) {
        sessionRepository.deleteByUserId(userId);
    }

    /**
     * Удаляет все просроченные сессии. Удобно вызывать по расписанию (@Scheduled)
     * из отдельного компонента, чтобы таблица sessions не разрасталась.
     */
    public void deleteExpiredSessions() {
        sessionRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}