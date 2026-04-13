package dio.todolist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dio.todolist.domain.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateCreated,
        TaskStatus status
) {}
