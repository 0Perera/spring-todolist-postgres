package dio.todolist.dto;

import dio.todolist.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record TaskUpdate(
        @Schema(description = "Novo título da tarefa. Omita o campo para não alterar.", example = "Revisar conteúdo")
        Optional<String> title,

        @Schema(description = "Nova descrição da tarefa. Omita o campo para não alterar.", example = "Rever os capítulos 3 e 4.")
        Optional<String> description,

        @Schema(description = "Novo status da tarefa. Omita o campo para não alterar.", example = "EM_ANDAMENTO")
        Optional<TaskStatus> status
) {}
