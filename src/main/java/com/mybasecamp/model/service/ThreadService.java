package com.mybasecamp.model.service;

import com.mybasecamp.model.entity.Thread;
import com.mybasecamp.model.repository.ThreadRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ThreadService {

    private final ThreadRepository threadRepository;

    public ThreadService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    public Map<String, Object> createThreadService(Integer projectId, String title, Integer createdBy) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (projectId == null || title == null || title.isBlank() || createdBy == null) {
                throw new IllegalArgumentException("Project ID, title, and creator ID are required");
            }
            Thread thread = new Thread();
            thread.setProjectId(projectId);
            thread.setTitle(title.trim());
            thread.setCreatedBy(createdBy);
            thread.setCreatedAt(LocalDateTime.now());
            thread.setUpdatedAt(LocalDateTime.now());
            Thread saved = threadRepository.save(thread);
            result.put("success", true);
            result.put("data", saved);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getThreadService(Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("Thread ID is required");
            }
            Thread thread = threadRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Thread not found"));
            result.put("success", true);
            result.put("data", thread);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getProjectThreadsService(Integer projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (projectId == null) {
                throw new IllegalArgumentException("Project ID is required");
            }
            List<Thread> threads = threadRepository.findByProjectId(projectId);
            result.put("success", true);
            result.put("data", threads);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> updateThreadService(Integer id, String title) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (id == null || title == null || title.isBlank()) {
                throw new IllegalArgumentException("Thread ID and title are required");
            }
            Thread thread = threadRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Thread not found"));
            thread.setTitle(title.trim());
            thread.setUpdatedAt(LocalDateTime.now());
            threadRepository.save(thread);
            result.put("success", true);
            result.put("message", "Thread updated successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> deleteThreadService(Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("Thread ID is required");
            }
            if (!threadRepository.existsById(id)) {
                throw new EntityNotFoundException("Thread not found");
            }
            threadRepository.deleteById(id);
            result.put("success", true);
            result.put("message", "Thread deleted successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }
}
