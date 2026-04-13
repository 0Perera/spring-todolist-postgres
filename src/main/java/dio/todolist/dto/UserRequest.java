package dio.todolist.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = "O nome é obrigatório")
        String name,
        @NotBlank(message = "O email é obrigatório")
        @Email
        String email,
        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}
