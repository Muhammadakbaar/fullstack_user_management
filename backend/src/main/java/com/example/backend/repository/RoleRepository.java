package com.example.backend.repository;

import com.example.backend.model.Role;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoleRepository extends R2dbcRepository<Role, UUID> {
    Mono<Role> findByName(String name);
} 