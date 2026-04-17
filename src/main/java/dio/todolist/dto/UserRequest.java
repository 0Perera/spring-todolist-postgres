package dio.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @Schema(description = "Nome de exibição do usuário.", example = "Felipe")
        @NotBlank(message = "O nome é obrigatório")
        String name,
        
        @Schema(description = "Endereço de e-mail que será utilizado para realizar o login no sistema.", example = "felipe@example.com")
        @NotBlank(message = "O email é obrigatório")
        @Email
        String email,
        
        @Schema(description = "Senha que será criptografada para acessar o sistema.", example = "senhaForte123")
        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}
