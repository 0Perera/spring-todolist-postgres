package dio.todolist.repository;

import dio.todolist.domain.Task;
import dio.todolist.domain.TaskStatus;
import dio.todolist.domain.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByTitleAndCreatedBy(String title, User createdBy);

    Page<Task> findByCreatedBy(User user, Pageable pageable);

    Page<Task> findByCreatedByAndStatus(User createdBy, TaskStatus status, Pageable pageable);
}
