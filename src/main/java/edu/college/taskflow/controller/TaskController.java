package edu.college.taskflow.controller;

import edu.college.taskflow.dto.*;
import edu.college.taskflow.model.*;
import edu.college.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    private final TaskService tasks;
    public TaskController(TaskService tasks) { this.tasks = tasks; }

    @GetMapping
    public List<TaskResponse> list(Authentication auth,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore) {
        return tasks.list(auth.getName(), status, priority, dueAfter, dueBefore);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(Authentication auth, @Valid @RequestBody TaskRequest request) {
        return tasks.create(auth.getName(), request);
    }

    @GetMapping("/{id}")
    public TaskResponse get(Authentication auth, @PathVariable Long id) {
        return tasks.get(auth.getName(), id);
    }

    @PutMapping("/{id}")
    public TaskResponse update(Authentication auth, @PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return tasks.update(auth.getName(), id, request);
    }

    @PatchMapping("/{id}/assignee")
    public TaskResponse assign(Authentication auth, @PathVariable Long id, @Valid @RequestBody AssignTaskRequest request) {
        return tasks.assign(auth.getName(), id, request.assigneeId());
    }

    @DeleteMapping("/{id}/assignee")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassign(Authentication auth, @PathVariable Long id) {
        tasks.unassign(auth.getName(), id);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(Authentication auth, @PathVariable Long id,
                                     @Valid @RequestBody UpdateStatusRequest request) {
        return tasks.updateStatus(auth.getName(), id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        tasks.delete(auth.getName(), id);
    }
}
