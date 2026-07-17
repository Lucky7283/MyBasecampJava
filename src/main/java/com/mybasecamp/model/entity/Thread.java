package com.mybasecamp.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MODEL — threads table.
 * Mirrors the Prisma "threads" model.
 * Using @Table(name="threads") because "Thread" collides with java.lang.Thread —
 * the class name is Thread but the table name is "threads".
 */
@Entity
@Table(name = "threads")
@Getter
@Setter
@NoArgsConstructor
public class Thread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(nullable = false)
    private String title;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User author;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Message> messages;

    @OneToMany(mappedBy = "thread",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<ThreadsMember> members;
}
