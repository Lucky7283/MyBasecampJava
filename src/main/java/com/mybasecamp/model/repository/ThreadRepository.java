package com.mybasecamp.model.repository;

import com.mybasecamp.model.entity.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ThreadRepository extends JpaRepository<Thread, Integer> {
    List<Thread> findByProjectId(Integer projectId);

}

