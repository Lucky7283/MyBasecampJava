package com.mybasecamp.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "thread_members")
@Getter
@Setter
@NoArgsConstructor
public class ThreadsMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne()
    @JoinColumn(name = "community")
    private User users;
    @ManyToOne()
    @JoinColumn(name = "thread_id")
    private Thread thread;

}
