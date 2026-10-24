package edu.college.taskflow.dto;
import edu.college.taskflow.model.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record TaskRequest(@NotBlank @Size(max=120) String title, @Size(max=2000) String description, TaskStatus status, TaskPriority priority, @FutureOrPresent LocalDate dueDate) {}
