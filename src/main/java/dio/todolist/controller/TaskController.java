package dio.todolist.controller;

import dio.todolist.domain.TaskStatus;
import dio.todolist.domain.User;
import dio.todolist.dto.TaskRequest;
import dio.todolist.dto.TaskResponse;
import dio.todolist.dto.TaskUpdate;
import dio.todolist.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;

@RestController
@RequestMapping(value = "/task")
@Tag(name = "Tarefas", description = "Gerenciamento das tarefas do usuário")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Criar Tarefa", description = "Adiciona uma tarefa à lista de afazeres do usuário autenticado.\n" +
            "O status padrão ao ser criada é sempre `PENDENTE` (Pendente).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping
    public ResponseEntity<TaskResponse> create(@RequestBody @Valid TaskRequest taskRequest,
                                               @AuthenticationPrincipal User authUser) {
        var response = taskService.create(taskRequest, authUser);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Listar todas as Tarefas", description = "Lista todas as tarefas criadas pelo e vinculadas ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tarefas retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> listAll(@AuthenticationPrincipal User authUser, Pageable pageable) {
        var response = taskService.listAll(authUser, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar pelo título", description = "Busca uma tarefa específica através da correspondência exata do título fornecido.\n" +
            "Útil para filtrar e encontrar uma tarefa rapidamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação no título enviado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @GetMapping("/findByTitle")
    public ResponseEntity<TaskResponse> findByTitle(@RequestParam String title,
                                                    @AuthenticationPrincipal User authUser) {
        var response = taskService.findByTitle(title, authUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar Tarefas por Status", description = "Lista e filtra as tarefas pelo seu status atual (`PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista filtrada retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TaskResponse>> listByStatus(@PathVariable TaskStatus status,
                                                           @AuthenticationPrincipal User authUser,
                                                           Pageable pageable) {
        var response = taskService.listByStatus(status, authUser, pageable);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Atualizar Tarefa", description = "Atualiza dinamicamente as informações de uma tarefa pelo seu ID numérico.\n" +
            "Apenas o próprio usuário autenticado pode alterar a sua tarefa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para alterar esta tarefa"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                               @RequestBody @Valid TaskUpdate taskUpdate,
                                               @AuthenticationPrincipal User authUser) {
        var response = taskService.update(id, taskUpdate, authUser);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Deletar Tarefa", description = "Remove e exclui permanentemente uma de suas tarefas a partir do ID correspondente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir esta tarefa"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal User authUser) {
        taskService.delete(id, authUser);
        return ResponseEntity.noContent().build();
    }

}
