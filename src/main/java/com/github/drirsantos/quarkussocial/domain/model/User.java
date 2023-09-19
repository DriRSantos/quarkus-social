package com.github.drirsantos.quarkussocial.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    @NotBlank(message = "name is required")
    private String name;
    @Column(name="age")
    @NotNull(message = "age is required")
    private Integer age;
}
