package edu.college.taskflow.dto;
import edu.college.taskflow.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
public record UpdateStatusRequest(@NotNull TaskStatus status) {}
