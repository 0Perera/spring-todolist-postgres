package dio.todolist.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dio.todolist.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record TaskResponse(
        @Schema(description = "Identificador único da tarefa.", example = "1")
        Long id,

        @Schema(description = "Título da tarefa.", example = "Estudar Java")
        String title,

        @Schema(description = "Descrição detalhada da tarefa.", example = "Ler do capítulo 1 ao 5 do livro.")
        String description,

        @Schema(description = "Data e hora de criação da tarefa.", example = "28-05-2025 10:30:00")
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dateCreated,

        @Schema(description = "Status atual da tarefa.", example = "PENDENTE")
        TaskStatus status
) {}
