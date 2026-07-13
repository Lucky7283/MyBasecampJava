package com.mybasecamp.model.repository;

import com.mybasecamp.model.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    List<Attachment> findByProjectId(Integer projectId);

    void deleteById(Integer id); // уже есть в JpaRepository

}
