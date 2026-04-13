package dio.todolist.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "O email deve ser válido")
        @NotBlank(message = "O email é obrigatório")
        String email,
        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}
