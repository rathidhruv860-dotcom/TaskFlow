package edu.college.taskflow.dto;
import jakarta.validation.constraints.NotNull;
public record AssignTaskRequest(@NotNull Long assigneeId) {}
