package dio.todolist.controller;

import dio.todolist.domain.TaskStatus;
import dio.todolist.dto.TaskRequest;
import dio.todolist.dto.TaskResponse;
import dio.todolist.dto.TaskUpdate;
import dio.todolist.handler.NotFoundException;
import dio.todolist.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

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
    @DisplayName("Deve retornar 201 Created ao criar uma tarefa com dados validos")
    void createCase1() throws Exception {
        TaskRequest createdRequest = createDefaultRequest();
        TaskResponse expectedResponse = createDefaultResponse();
        String taskJson = objectMapper.writeValueAsString(createdRequest);

        when(taskService.create(createdRequest)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao criar uma tarefa com dados invalidos")
    void createCase2() throws Exception {
        TaskRequest createdRequest = new TaskRequest("", "");
        String taskJson = objectMapper.writeValueAsString(createdRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao buscar por título existente")
    void findByTitleCase1() throws Exception {
        TaskRequest createdRequest = createDefaultRequest();
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskService.findByTitle(createdRequest)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/task/findByTitle")
                    .param("title", DEFAULT_TITLE)
                    .param("description", DEFAULT_DESCRIPTION)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao buscar por título inexistente")
    void findByTitleCase2() throws Exception {
        TaskRequest createdRequest = createDefaultRequest();
        when(taskService.findByTitle(createdRequest)).thenThrow(new NotFoundException("Tarefa não encontrada"));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/findByTitle")
                    .param("title", DEFAULT_TITLE)
                    .param("description", DEFAULT_DESCRIPTION)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao listar todas as tarefas")
    void listAllCase1() throws Exception {
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskService.listAll()).thenReturn(List.of(expectedResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/task"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK e lista vazia ao listar todas as tarefas quando não houver tarefas cadastradas")
    void listAllCase2() throws Exception {
        when(taskService.listAll()).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/task"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao listar tarefas por status PENDENTE")
    void listByStatusCase1() throws Exception {
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskService.listByStatus(TaskStatus.PENDENTE)).thenReturn(List.of(expectedResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/status/PENDENTE"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao listar tarefas por status CONCLUIDA")
    void listByStatusCase2() throws Exception {
        TaskResponse expectedResponse = new TaskResponse(
                DEFAULT_ID,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                LocalDateTime.now(),
                TaskStatus.CONCLUIDA
        );

        when(taskService.listByStatus(TaskStatus.CONCLUIDA)).thenReturn(List.of(expectedResponse));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/status/CONCLUIDA"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK e lista vazia ao listar tarefas por status quando não houver tarefas nesse status")
    void listByStatusCase3() throws Exception {
        when(taskService.listByStatus(TaskStatus.PENDENTE)).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/task/status/PENDENTE"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao atualizar uma tarefa com dados validos")
    void updateCase1() throws Exception {
        TaskUpdate updateRequest = new TaskUpdate(
                java.util.Optional.of("Nova tarefa"),
                java.util.Optional.of("Nova descrição"),
                java.util.Optional.of(TaskStatus.CONCLUIDA)
        );
        TaskResponse expectedResponse = new TaskResponse(
                DEFAULT_ID,
                "Nova tarefa",
                "Nova descrição",
                LocalDateTime.now(),
                TaskStatus.CONCLUIDA
        );
        String taskJson = objectMapper.writeValueAsString(updateRequest);

        when(taskService.update(DEFAULT_ID, updateRequest)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/task/" + DEFAULT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao atualizar apenas o título de uma tarefa")
    void updateCase2() throws Exception {
        TaskUpdate updateRequest = new TaskUpdate(
                java.util.Optional.of("Título atualizado"),
                java.util.Optional.empty(),
                java.util.Optional.empty()
        );
        TaskResponse expectedResponse = new TaskResponse(
                DEFAULT_ID,
                "Título atualizado",
                DEFAULT_DESCRIPTION,
                LocalDateTime.now(),
                TaskStatus.PENDENTE
        );
        String taskJson = objectMapper.writeValueAsString(updateRequest);

        when(taskService.update(DEFAULT_ID, updateRequest)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/task/" + DEFAULT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao atualizar apenas o status de uma tarefa")
    void updateCase3() throws Exception {
        TaskUpdate updateRequest = new TaskUpdate(
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.of(TaskStatus.CONCLUIDA)
        );
        TaskResponse expectedResponse = new TaskResponse(
                DEFAULT_ID,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                LocalDateTime.now(),
                TaskStatus.CONCLUIDA
        );
        String taskJson = objectMapper.writeValueAsString(updateRequest);

        when(taskService.update(DEFAULT_ID, updateRequest)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/task/" + DEFAULT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(taskJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 204 No Content ao deletar uma tarefa existente")
    void deleteCase1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/task/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 204 No Content ao deletar a última tarefa")
    void deleteCase2() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/task/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao deletar uma tarefa inexistente")
    void deleteCase3() throws Exception {
        doThrow(new NotFoundException("Tarefa não encontrada")).when(taskService).delete(DEFAULT_ID);

        mockMvc.perform(MockMvcRequestBuilders.delete("/task/" + DEFAULT_ID)).
                andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

}