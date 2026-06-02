package dio.todolist.controller;

import dio.todolist.domain.User;
import dio.todolist.dto.LoginRequest;
import dio.todolist.dto.LoginResponse;
import dio.todolist.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para login, logout e informações da sessão")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @SecurityRequirements
    @Operation(summary = "Realizar Login", description = "Autentica um usuário e retorna um token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        User user = (User) auth.getPrincipal();
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @SecurityRequirements
    @Operation(summary = "Realizar Logout", description = "Encerra a sessão do lado do cliente. Como a autenticação é stateless (JWT), basta descartar o token.")
    @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso")
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logout realizado! Descarte o token no lado do cliente.");
    }

    @Operation(summary = "Consultar Sessão", description = "Retorna o e-mail do usuário autenticado pelo token JWT enviado no header.")
    @ApiResponse(responseCode = "200", description = "Retorna o e-mail do usuário ativo ou mensagem de ausência")
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nenhum usuário autenticado");
        }

        return ResponseEntity.ok(auth.getName());
    }
}
