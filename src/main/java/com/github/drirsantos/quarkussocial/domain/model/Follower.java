package com.github.drirsantos.quarkussocial.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.mapping.Join;

@Entity
@Table(name="followers")
@Data
public class Follower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="follower_id")
    private User follower;


}
