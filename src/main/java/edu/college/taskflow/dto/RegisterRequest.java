package edu.college.taskflow.dto;
import jakarta.validation.constraints.*;
public record RegisterRequest(@NotBlank @Size(max=80) String name, @NotBlank @Email @Size(max=160) String email, @NotBlank @Size(min=8,max=72) String password) {}
