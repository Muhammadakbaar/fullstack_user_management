package com.example.backend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table("users")
public class User {
    @Id
    private UUID id;

    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 50)
    @Column("username")
    private String username;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email
    @Size(max = 100)
    @Column("email")
    private String email;

    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 6, max = 100)
    @Column("password")
    private String password;

    @NotNull(message = "Role ID tidak boleh kosong")
    @Column("role_id")
    private UUID roleId;
}