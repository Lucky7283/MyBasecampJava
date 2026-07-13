package com.mybasecamp.model.service;

import com.mybasecamp.model.entity.Attachment;
import com.mybasecamp.model.repository.AttachmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public Map<String, Object> createAttachmentService(Integer projectId, String filename, String original, String mimeType, Long size) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (projectId == null || filename == null || filename.isBlank() || original == null || original.isBlank() || mimeType == null || mimeType.isBlank()) {
                throw new IllegalArgumentException("Missing required attachment parameters");
            }
            Attachment attachment = new Attachment();
            attachment.setProjectId(projectId);
            attachment.setFilename(filename);
            attachment.setOriginal(original);
            attachment.setMimeType(mimeType);
            attachment.setSize(size != null ? size : 0L);
            attachment.setCreatedAt(LocalDateTime.now());
            Attachment saved = attachmentRepository.save(attachment);
            result.put("success", true);
            result.put("data", saved);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getAttachmentService(Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("Attachment ID is required");
            }
            Attachment attachment = attachmentRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
            result.put("success", true);
            result.put("data", attachment);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getProjectAttachmentsService(Integer projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (projectId == null) {
                throw new IllegalArgumentException("Project ID is required");
            }
            List<Attachment> attachments = attachmentRepository.findByProjectId(projectId);
            result.put("success", true);
            result.put("data", attachments);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> deleteAttachmentService(Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (id == null) {
                throw new IllegalArgumentException("Attachment ID is required");
            }
            if (!attachmentRepository.existsById(id)) {
                throw new EntityNotFoundException("Attachment not found");
            }
            attachmentRepository.deleteById(id);
            result.put("success", true);
            result.put("message", "Attachment deleted successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }
}
