package dio.todolist.controller;

import dio.todolist.domain.User;
import dio.todolist.dto.UserRequest;
import dio.todolist.dto.UserResponse;
import dio.todolist.dto.UserUpdate;
import dio.todolist.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;

@RestController
@RequestMapping(value = "/user")
@Tag(name = "Usuários", description = "Gerenciamento dos usuários do sistema")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {this.userService = userService;}


    @SecurityRequirements
    @Operation(summary = "Criar Usuário", description = "Cadastra um novo usuário no sistema.\n" +
            "Este é o primeiro passo recomendado para utilizar a API, pois as rotas de tarefas exigem autenticação baseada nos dados cadastrados aqui.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação (campos inválidos ou não preenchidos)"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Valid UserRequest userRequest) {
        var response = userService.create(userRequest);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Buscar Usuário por ID", description = "Retorna os detalhes de um usuário específico utilizando seu identificador único (ID).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar este usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id, @AuthenticationPrincipal User authUser) {
        var response = userService.findById(id, authUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar Usuário", description = "Muda os dados e informações do usuário a partir do seu ID numérico.\n" +
            "Você pode alterar o nome, email ou senha de acesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar este usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "E-mail já utilizado por outro usuário")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody @Valid UserUpdate userUpdate, @AuthenticationPrincipal User authUser) {
        var response = userService.update(id, userUpdate, authUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deletar Usuário", description = "Exclui um usuário do sistema permanentemente a partir do identificador único (ID).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir este usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User authUser) {
        userService.delete(id, authUser);
        return ResponseEntity.noContent().build();
    }
}
