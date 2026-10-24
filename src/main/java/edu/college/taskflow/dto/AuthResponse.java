package edu.college.taskflow.dto;
public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserResponse user) {}
