package edu.college.taskflow.repository;
import edu.college.taskflow.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
public interface TaskRepository extends JpaRepository<Task,Long> {
    @Query("select t from Task t left join t.assignee a where (t.owner.id = :userId or a.id = :userId) " +
           "and (:status is null or t.status = :status) " +
           "and (:priority is null or t.priority = :priority) " +
           "and (:dueAfter is null or t.dueDate >= :dueAfter) " +
           "and (:dueBefore is null or t.dueDate <= :dueBefore) " +
           "order by t.createdAt desc")
    java.util.List<Task> findVisibleTasks(@Param("userId") Long userId,
                                          @Param("status") TaskStatus status,
                                          @Param("priority") TaskPriority priority,
                                          @Param("dueAfter") LocalDate dueAfter,
                                          @Param("dueBefore") LocalDate dueBefore);
    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
    @Query("select t from Task t left join t.assignee a where t.id = :id and (t.owner.id = :userId or a.id = :userId)")
    Optional<Task> findVisibleTask(@Param("id") Long id, @Param("userId") Long userId);
}
