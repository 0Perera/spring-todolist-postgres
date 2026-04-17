package dio.todolist.controller;

import dio.todolist.dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para login, logout e informações da sessão")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Realizar Login", description = "Autentica um usuário e cria uma sessão ativa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

        Authentication auth = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        return ResponseEntity.ok("Login realizado! Bem-vindo, " + loginRequest.email());
    }

    @Operation(summary = "Realizar Logout", description = "Encerra a sessão do usuário autal")
    @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso")
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout realizado!");
    }

    @Operation(summary = "Consultar Sessão", description = "Retorna qual usuário está logado atualmente no sistema")
    @ApiResponse(responseCode = "200", description = "Retorna o nome do usuário ativo ou mensagem de ausência")
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.ok("Nenhum usuário autenticado");
        }

        return ResponseEntity.ok(auth.getName());
    }

}
