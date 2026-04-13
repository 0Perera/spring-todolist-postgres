package dio.todolist.dto;

import dio.todolist.domain.Task;
import dio.todolist.domain.TaskStatus;

import java.util.Optional;

public record TaskUpdate(
        Optional<String> title,
        Optional<String> description,
        Optional<TaskStatus> status
) {}
