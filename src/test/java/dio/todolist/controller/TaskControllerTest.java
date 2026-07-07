package dio.todolist.controller;

import dio.todolist.domain.TaskStatus;
import dio.todolist.dto.TaskRequest;
import dio.todolist.dto.TaskResponse;
import dio.todolist.dto.TaskUpdate;
import dio.todolist.handler.AccessDeniedException;
import dio.todolist.handler.NotFoundException;
import dio.todolist.service.JwtService;
import dio.todolist.service.TaskService;
import dio.todolist.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@WebMvcTest(controllers = TaskController.class)
class TaskControllerTest {

    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_TITLE = "Estudar Spring Boot";
    private static final String DEFAULT_DESCRIPTION = "Estudar por 5 horas";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TaskService taskService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    private TaskRequest createDefaultRequest() {
        return new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    private TaskResponse createDefaultResponse() {
        return new TaskResponse(
                DEFAULT_ID,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                LocalDateTime.now(),
                TaskStatus.PENDENTE
        );
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 201 Created ao criar uma tarefa com dados válidos")
    void createCase1() throws Exception {
        TaskRequest request = createDefaultRequest();
        TaskResponse expectedResponse = createDefaultResponse();
        String taskJson = objectMapper.writeValueAsString(request);

        when(taskService.create(any(TaskRequest.class), any())).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 400 Bad Request ao criar uma tarefa com dados inválidos")
    void createCase2() throws Exception {
        String taskJson = objectMapper.writeValueAsString(new TaskRequest("", ""));

        mockMvc.perform(MockMvcRequestBuilders.post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao buscar por título existente")
    void findByTitleCase1() throws Exception {
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskService.findByTitle(eq(DEFAULT_TITLE), any())).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/task/findByTitle")
                        .param("title", DEFAULT_TITLE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 Not Found ao buscar por título inexistente")
    void findByTitleCase2() throws Exception {
        when(taskService.findByTitle(eq(DEFAULT_TITLE), any())).thenThrow(new NotFoundException("Tarefa não encontrada"));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/findByTitle")
                        .param("title", DEFAULT_TITLE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao listar todas as tarefas")
    void listAllCase1() throws Exception {
        TaskResponse expectedResponse = createDefaultResponse();
        Page<TaskResponse> page = new PageImpl<>(List.of(expectedResponse));

        when(taskService.listAll(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/task"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK e lista vazia quando não houver tarefas")
    void listAllCase2() throws Exception {
        when(taskService.listAll(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(MockMvcRequestBuilders.get("/task"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao listar tarefas por status PENDENTE")
    void listByStatusCase1() throws Exception {
        when(taskService.listByStatus(eq(TaskStatus.PENDENTE), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(createDefaultResponse())));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/status/PENDENTE"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao listar tarefas por status CONCLUIDA")
    void listByStatusCase2() throws Exception {
        TaskResponse concluded = new TaskResponse(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_DESCRIPTION, LocalDateTime.now(), TaskStatus.CONCLUIDA);
        when(taskService.listByStatus(eq(TaskStatus.CONCLUIDA), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(concluded)));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/status/CONCLUIDA"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK e lista vazia quando não houver tarefas com o status")
    void listByStatusCase3() throws Exception {
        when(taskService.listByStatus(eq(TaskStatus.PENDENTE), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/status/PENDENTE"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao atualizar uma tarefa com todos os campos")
    void updateCase1() throws Exception {
        TaskUpdate updateRequest = new TaskUpdate(
                Optional.of("Nova tarefa"),
                Optional.of("Nova descrição"),
                Optional.of(TaskStatus.CONCLUIDA));
        TaskResponse expectedResponse = new TaskResponse(DEFAULT_ID, "Nova tarefa", "Nova descrição", LocalDateTime.now(), TaskStatus.CONCLUIDA);
        String taskJson = objectMapper.writeValueAsString(updateRequest);

        when(taskService.update(eq(DEFAULT_ID), any(TaskUpdate.class), any())).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/task/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao atualizar apenas o título de uma tarefa")
    void updateCase2() throws Exception {
        TaskUpdate updateRequest = new TaskUpdate(Optional.of("Título atualizado"), Optional.empty(), Optional.empty());
        TaskResponse expectedResponse = new TaskResponse(DEFAULT_ID, "Título atualizado", DEFAULT_DESCRIPTION, LocalDateTime.now(), TaskStatus.PENDENTE);
        String taskJson = objectMapper.writeValueAsString(updateRequest);

        when(taskService.update(eq(DEFAULT_ID), any(TaskUpdate.class), any())).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/task/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao atualizar apenas o status de uma tarefa")
    void updateCase3() throws Exception {
        TaskUpdate updateRequest = new TaskUpdate(Optional.empty(), Optional.empty(), Optional.of(TaskStatus.CONCLUIDA));
        TaskResponse expectedResponse = new TaskResponse(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_DESCRIPTION, LocalDateTime.now(), TaskStatus.CONCLUIDA);
        String taskJson = objectMapper.writeValueAsString(updateRequest);

        when(taskService.update(eq(DEFAULT_ID), any(TaskUpdate.class), any())).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/task/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 Not Found ao tentar atualizar tarefa inexistente")
    void updateCase4() throws Exception {
        TaskUpdate updateRequest = new TaskUpdate(Optional.of("Título"), Optional.empty(), Optional.empty());
        String taskJson = objectMapper.writeValueAsString(updateRequest);

        when(taskService.update(eq(DEFAULT_ID), any(TaskUpdate.class), any())).thenThrow(new NotFoundException("Tarefa não encontrada"));

        mockMvc.perform(MockMvcRequestBuilders.put("/task/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 403 Forbidden ao tentar atualizar tarefa de outro usuário")
    void updateCase5() throws Exception {
        TaskUpdate updateRequest = new TaskUpdate(Optional.of("Título"), Optional.empty(), Optional.empty());
        String taskJson = objectMapper.writeValueAsString(updateRequest);

        when(taskService.update(eq(DEFAULT_ID), any(TaskUpdate.class), any())).thenThrow(new AccessDeniedException("Sem permissão"));

        mockMvc.perform(MockMvcRequestBuilders.put("/task/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 204 No Content ao deletar uma tarefa existente")
    void deleteCase1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/task/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 403 Forbidden ao deletar tarefa de outro usuário")
    void deleteCase2() throws Exception {
        doThrow(new AccessDeniedException("Acesso negado")).when(taskService).delete(eq(DEFAULT_ID), any());

        mockMvc.perform(MockMvcRequestBuilders.delete("/task/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 Not Found ao deletar uma tarefa inexistente")
    void deleteCase3() throws Exception {
        doThrow(new NotFoundException("Tarefa não encontrada")).when(taskService).delete(eq(DEFAULT_ID), any());

        mockMvc.perform(MockMvcRequestBuilders.delete("/task/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }
}
