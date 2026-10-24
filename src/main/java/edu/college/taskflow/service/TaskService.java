package edu.college.taskflow.service;

import edu.college.taskflow.dto.*;
import edu.college.taskflow.model.*;
import edu.college.taskflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository tasks;
    private final UserRepository users;

    public TaskService(TaskRepository tasks, UserRepository users) {
        this.tasks = tasks;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(String email, TaskStatus status, TaskPriority priority,
                                   LocalDate dueAfter, LocalDate dueBefore) {
        AppUser current = user(email);
        return tasks.findVisibleTasks(current.getId(), status, priority, dueAfter, dueBefore)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskResponse create(String email, TaskRequest request) {
        Task task = new Task(request.title().trim(), request.description(),
                request.priority() == null ? TaskPriority.MEDIUM : request.priority(),
                request.dueDate(), user(email));
        if (request.status() != null) task.updateStatus(request.status());
        return toResponse(tasks.save(task));
    }

    @Transactional(readOnly = true)
    public TaskResponse get(String email, Long id) {
        return toResponse(findVisible(email, id));
    }

    @Transactional
    public TaskResponse update(String email, Long id, TaskRequest request) {
        Task task = findOwned(email, id);
        task.update(request.title().trim(), request.description(),
                request.status() == null ? TaskStatus.TODO : request.status(),
                request.priority() == null ? TaskPriority.MEDIUM : request.priority(), request.dueDate());
        return toResponse(tasks.save(task));
    }

    @Transactional
    public TaskResponse assign(String email, Long id, Long assigneeId) {
        Task task = findOwned(email, id);
        AppUser assignee = users.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found."));
        task.assignTo(assignee);
        return toResponse(tasks.save(task));
    }

    @Transactional
    public TaskResponse unassign(String email, Long id) {
        Task task = findOwned(email, id);
        task.assignTo(null);
        return toResponse(tasks.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(String email, Long id, TaskStatus status) {
        Task task = findVisible(email, id);
        task.updateStatus(status);
        return toResponse(tasks.save(task));
    }

    @Transactional
    public void delete(String email, Long id) {
        tasks.delete(findOwned(email, id));
    }

    private AppUser user(String email) {
        return users.findByEmail(email).orElseThrow(
                () -> new IllegalStateException("Authenticated user no longer exists."));
    }

    private Task findOwned(String email, Long id) {
        AppUser current = user(email);
        return tasks.findByIdAndOwnerId(id, current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found or you are not its owner."));
    }

    private Task findVisible(String email, Long id) {
        AppUser current = user(email);
        return tasks.findVisibleTask(id, current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));
    }

    private TaskResponse toResponse(Task task) {
        AppUser assignee = task.getAssignee();
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(),
                task.getPriority(), task.getDueDate(), task.getOwner().getId(),
                assignee == null ? null : assignee.getId(), assignee == null ? null : assignee.getName(),
                task.getCreatedAt(), task.getUpdatedAt());
    }
}
