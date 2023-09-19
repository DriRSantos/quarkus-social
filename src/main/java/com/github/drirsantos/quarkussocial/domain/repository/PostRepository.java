package com.github.drirsantos.quarkussocial.domain.repository;

import com.github.drirsantos.quarkussocial.domain.model.Post;
import com.github.drirsantos.quarkussocial.domain.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PostRepository implements PanacheRepository<Post> {
}
