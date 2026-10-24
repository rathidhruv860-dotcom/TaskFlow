package edu.college.taskflow.dto;
import edu.college.taskflow.model.*;
import java.time.*;
public record TaskResponse(Long id, String title, String description, TaskStatus status, TaskPriority priority,
                           LocalDate dueDate, Long ownerId, Long assigneeId, String assigneeName,
                           Instant createdAt, Instant updatedAt) {}
