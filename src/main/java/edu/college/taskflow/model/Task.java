package edu.college.taskflow.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tasks", indexes = {@Index(name="idx_task_owner_status", columnList="owner_id,status"), @Index(name="idx_task_due_date", columnList="due_date")})
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=120) private String title;
    @Column(length=2000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private TaskStatus status = TaskStatus.TODO;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private TaskPriority priority = TaskPriority.MEDIUM;
    @Column(name="due_date") private LocalDate dueDate;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="owner_id", nullable=false) private AppUser owner;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assignee_id") private AppUser assignee;
    @Column(nullable=false, updatable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;
    protected Task() {}
    public Task(String title, String description, TaskPriority priority, LocalDate dueDate, AppUser owner) {
        this.title=title; this.description=description; this.priority=priority; this.dueDate=dueDate; this.owner=owner;
        this.createdAt=Instant.now(); this.updatedAt=this.createdAt;
    }
    @PreUpdate void touch(){updatedAt=Instant.now();}
    public void update(String title,String description,TaskStatus status,TaskPriority priority,LocalDate dueDate){
        this.title=title; this.description=description; this.status=status; this.priority=priority; this.dueDate=dueDate;
    }
    public void assignTo(AppUser assignee){this.assignee=assignee;}
    public void updateStatus(TaskStatus status){this.status=status;}
    public Long getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;}
    public TaskStatus getStatus(){return status;} public TaskPriority getPriority(){return priority;} public LocalDate getDueDate(){return dueDate;}
    public AppUser getOwner(){return owner;} public AppUser getAssignee(){return assignee;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
