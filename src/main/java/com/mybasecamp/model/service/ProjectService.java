package com.mybasecamp.model.service;

import com.mybasecamp.model.entity.Project;
import com.mybasecamp.model.entity.User;
import com.mybasecamp.model.repository.ProjectRepository;
import com.mybasecamp.model.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> createProjectService(String name, String description, Integer ownerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (name == null || name.isBlank() || ownerId == null) {
                throw new IllegalArgumentException("Project name and owner ID are required");
            }
            User user = userRepository.findById(ownerId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            Project project = new Project();
            project.setName(name);
            project.setDescription(description != null ? description : "");
            project.setOwnerId(ownerId);
            project.setCreatedAt(LocalDateTime.now());
            project.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(project);
            result.put("success", true);
            result.put("message", "Project created successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getProjectService(Integer projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (projectId == null) {
                throw new IllegalArgumentException("Project ID is required");
            }
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));
            result.put("success", true);
            result.put("data", project);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getUserProjectsService(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            List<Project> projects = projectRepository.findByOwnerId(userId);
            result.put("success", true);
            result.put("data", projects);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getAllProjectsService() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Project> projects = projectRepository.findAll();
            result.put("success", true);
            result.put("data", projects);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> updateProjectService(Integer projectId, String name, String description) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (projectId == null || name == null || name.isBlank()) {
                throw new IllegalArgumentException("Project ID and name are required");
            }
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));
            project.setName(name);
            project.setDescription(description != null ? description : "");
            project.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(project);
            result.put("success", true);
            result.put("message", "Project updated successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> deleteProjectService(Integer projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (projectId == null) {
                throw new IllegalArgumentException("Project ID is required");
            }
            if (!projectRepository.existsById(projectId)) {
                throw new EntityNotFoundException("Project not found");
            }
            projectRepository.deleteById(projectId);
            result.put("success", true);
            result.put("message", "Project deleted successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }
}
