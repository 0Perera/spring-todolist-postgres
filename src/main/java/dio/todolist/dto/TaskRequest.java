package dio.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record TaskRequest(
        @Schema(description = "Título curto que identifica o objetivo geral.", example = "Estudar Java")
        @NotBlank(message = "O título é obrigatório.")
        String title,
        
        @Schema(description = "Informa detalhes sobre aquele objetivo.", example = "Ler do capítulo 1 ao 5 do livro.")
        String description
) {}
