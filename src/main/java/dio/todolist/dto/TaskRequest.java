package dio.todolist.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskRequest(
        @NotBlank(message = "O título é obrigatório.")
        String title,
        String description
) {}
