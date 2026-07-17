package com.mybasecamp.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MODEL — users table.
 * Mirrors the Prisma "users" model.
 * admin field: 0 = regular user, 1 = admin (same convention as Node.js project).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * 0 = regular user, 1 = admin
     */
    @Column(nullable = false)
    private Integer admin = 0;


    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Project> projects;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Session> sessions;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Thread> threads;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    @OneToMany(mappedBy = "users",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ThreadsMember> community;



    public boolean isAdmin() {
        return Integer.valueOf(1).equals(this.admin);
    }
}
