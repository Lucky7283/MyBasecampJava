package com.mybasecamp.model.service;

import com.mybasecamp.model.entity.Message;
import com.mybasecamp.model.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Map<String, Object> createMessageService(Integer threadId, Integer authorId, String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (threadId == null || authorId == null || content == null || content.isBlank()) {
                throw new IllegalArgumentException("Thread ID, author ID, and content are required");
            }
            Message message = new Message();
            message.setThreadId(threadId);
            message.setAuthorId(authorId);
            message.setContent(content.trim());
            message.setCreatedAt(LocalDateTime.now());
            message.setUpdatedAt(LocalDateTime.now());
            Message saved = messageRepository.save(message);
            result.put("success", true);
            result.put("data", saved);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getMessageService(Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("Message ID is required");
            }
            Message message = messageRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Message not found"));
            result.put("success", true);
            result.put("data", message);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getThreadMessagesService(Integer threadId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (threadId == null) {
                throw new IllegalArgumentException("Thread ID is required");
            }
            List<Message> messages = messageRepository.findByThreadId(threadId);
            result.put("success", true);
            result.put("data", messages);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> updateMessageService(Integer id, String content) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (id == null || content == null || content.isBlank()) {
                throw new IllegalArgumentException("Message ID and content are required");
            }
            Message message = messageRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Message not found"));
            message.setContent(content.trim());
            message.setUpdatedAt(LocalDateTime.now());
            messageRepository.save(message);
            result.put("success", true);
            result.put("message", "Message updated successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> deleteMessageService(Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("Message ID is required");
            }
            if (!messageRepository.existsById(id)) {
                throw new EntityNotFoundException("Message not found");
            }
            messageRepository.deleteById(id);
            result.put("success", true);
            result.put("message", "Message deleted successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }
}
