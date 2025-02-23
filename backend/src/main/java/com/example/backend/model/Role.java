package com.example.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("roles")
public class Role {
    @Id
    private UUID id;

    @NotBlank(message = "Nama role tidak boleh kosong")
    @Size(max = 50, message = "Nama role tidak boleh lebih dari 50 karakter")
    @Column("name")
    private String name;
}