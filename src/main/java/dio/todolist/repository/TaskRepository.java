package dio.todolist.repository;

import dio.todolist.domain.Task;
import dio.todolist.domain.TaskStatus;
import dio.todolist.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByTitleAndCreatedBy(String title, User createdBy);

    List<Task> findByCreatedBy(User user);

    List<Task> findByCreatedByAndStatus(User createdBy, TaskStatus status);
}
