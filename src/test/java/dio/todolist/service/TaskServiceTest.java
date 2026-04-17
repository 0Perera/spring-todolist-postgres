package dio.todolist.service;

import dio.todolist.domain.TaskStatus;
import dio.todolist.domain.User;
import dio.todolist.dto.TaskRequest;
import dio.todolist.dto.TaskResponse;
import dio.todolist.domain.Task;
import dio.todolist.dto.TaskUpdate;
import dio.todolist.handler.AccessDeniedException;
import dio.todolist.handler.NotFoundException;
import dio.todolist.mapper.TaskMapper;
import dio.todolist.repository.TaskRepository;
import dio.todolist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@WithMockUser(username = "felipe@dev.com")
class TaskServiceTest {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final String DEFAULT_USER_USERNAME = "felipe@dev.com";
    private static final Long NOT_FOUND_ID = 9999L;
    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_TITLE = "Estudar Spring Boot";
    private static final String DEFAULT_DESCRIPTION = "Estudar por 5 horas";

    @MockitoBean
    TaskRepository taskRepository;

    @MockitoBean
    TaskMapper taskMapper;

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    TaskService taskService;

    private User authUser;

    private TaskRequest createDefaultRequest() {
        return new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    private TaskUpdate createDefaultUpdate() {
        return new TaskUpdate(
                Optional.empty(),
                Optional.of("Estudar por 3 horas"),
                Optional.of(TaskStatus.CONCLUIDA));
    }

    private Task createDefaultEntity() {
        Task task = new Task(createDefaultRequest());
        task.setId(DEFAULT_ID);
        return task;
    }

    private TaskResponse createDefaultResponse() {
        return new TaskResponse(DEFAULT_ID,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                LocalDateTime.now(),
                TaskStatus.PENDENTE);
    }

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(DEFAULT_USER_ID);
        user.setEmail(DEFAULT_USER_USERNAME);

        authUser = user;

        when(userRepository.findByEmail(DEFAULT_USER_USERNAME)).thenReturn(Optional.of(authUser));
    }

    @Test
    @DisplayName("Deve criar uma tarefa com sucesso")
    void createCase1() {
        TaskRequest createdRequest = createDefaultRequest();
        Task createdTask = createDefaultEntity();
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskMapper.toEntity(createdRequest)).thenReturn(createdTask);
        when(taskRepository.save(any(Task.class))).thenReturn(createdTask);
        when(taskMapper.toResponse(createdTask)).thenReturn(expectedResponse);

        TaskResponse result = taskService.create(createdRequest);

        assertEquals(expectedResponse, result);
        verify(taskMapper, times(1)).toEntity(createdRequest);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskMapper, times(1)).toResponse(createdTask);
    }

    @Test
    @DisplayName("Deve criar uma tarefa sem descrição")
    void createCase2() {
        TaskRequest createdRequest = new TaskRequest(DEFAULT_TITLE, null);
        Task createdTask = createDefaultEntity();
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskMapper.toEntity(createdRequest)).thenReturn(createdTask);
        when(taskRepository.save(createdTask)).thenReturn(createdTask);
        when(taskMapper.toResponse(createdTask)).thenReturn(expectedResponse);

        TaskResponse result = taskService.create(createdRequest);

        assertEquals(expectedResponse, result);
        verify(taskMapper, times(1)).toEntity(createdRequest);
        verify(taskRepository, times(1)).save(createdTask);
        verify(taskMapper, times(1)).toResponse(createdTask);
    }

    @Test
    @DisplayName("Deve encontrar uma tarefa por título com sucesso")
    void findByTitleCase1(){
        Task createdTask = createDefaultEntity();
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskRepository.findByTitleAndCreatedBy(eq(DEFAULT_TITLE), any(User.class))).thenReturn(Optional.of(createdTask));
        when(taskMapper.toResponse(createdTask)).thenReturn(expectedResponse);

        TaskResponse result = taskService.findByTitle(DEFAULT_TITLE);

        assertEquals(expectedResponse, result);
        verify(taskRepository, times(1)).findByTitleAndCreatedBy(eq(DEFAULT_TITLE), any(User.class));
        verify(taskMapper, times(1)).toResponse(createdTask);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando não encontrar tarefa por título")
    void findByTitleCase2(){
        when(taskRepository.findByTitleAndCreatedBy(eq(DEFAULT_TITLE), any(User.class))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.findByTitle(DEFAULT_TITLE));
        verify(taskRepository, times(1)).findByTitleAndCreatedBy(eq(DEFAULT_TITLE), any(User.class));
    }

    @Test
    @DisplayName("Deve listar todas as tarefas do usuário autenticado")
    void listAllCase1(){
        Task createdTask = createDefaultEntity();
        TaskResponse expectedResponse1 = createDefaultResponse();
        TaskResponse expectedResponse2 = new TaskResponse(2L,
                "Outra Tarefa",
                "Descrição de outra tarefa",
                LocalDateTime.now(),
                TaskStatus.PENDENTE);
        List<TaskResponse> expectedResponseList = List.of(expectedResponse1, expectedResponse2);

        when(taskRepository.findByCreatedBy(any(User.class))).thenReturn(List.of(createdTask));
        when(taskMapper.toResponseList(List.of(createdTask))).thenReturn(expectedResponseList);

        List<TaskResponse> result = taskService.listAll();

        assertEquals(expectedResponseList, result);
        verify(taskRepository, times(1)).findByCreatedBy(any(User.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário autenticado não tiver tarefas")
    void listAllCase2(){
        when(taskRepository.findByCreatedBy(any(User.class))).thenReturn(List.of());
        when(taskMapper.toResponseList(List.of())).thenReturn(List.of());

        List<TaskResponse> result = taskService.listAll();

        assertEquals(List.of(), result);
        verify(taskRepository, times(1)).findByCreatedBy(any(User.class));
    }

    @Test
    @DisplayName("Deve atualizar o status de uma tarefa com sucesso")
    void updateCase1() {
        TaskUpdate updateRequest = createDefaultUpdate();
        Task createdTask = createDefaultEntity();
        createdTask.setCreatedBy(this.authUser);
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(createdTask));
        when(taskRepository.save(createdTask)).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskMapper.toResponse(createdTask)).thenReturn(expectedResponse);

        TaskResponse result = taskService.update(createdTask.getId(), updateRequest);

        assertEquals(expectedResponse, result);
        verify(taskRepository, times(1)).findById(DEFAULT_ID);
        verify(taskRepository, times(1)).save(createdTask);
        verify(taskMapper, times(1)).toResponse(createdTask);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar atualizar tarefa inexistente")
    void updateCase2(){
        TaskUpdate updateRequest = createDefaultUpdate();

        when(taskRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.update(NOT_FOUND_ID, updateRequest));
        verify(taskRepository, times(1)).findById(NOT_FOUND_ID);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao tentar atualizar tarefa de outro usuário")
    void updateCase3() {
        User differentUser = new User();
        differentUser.setId(999L);
        differentUser.setEmail("outro@dev.com");

        Task taskFromAnotherUser = createDefaultEntity();
        taskFromAnotherUser.setCreatedBy(differentUser);

        TaskUpdate updateRequest = createDefaultUpdate();

        when(taskRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(taskFromAnotherUser));

        assertThrows(AccessDeniedException.class,
                () -> taskService.update(DEFAULT_ID, updateRequest));
        verify(taskRepository, times(1)).findById(DEFAULT_ID);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Deve listar tarefas por status com sucesso")
    void listByStatusCase1() {
        Task createdTask = createDefaultEntity();
        TaskResponse expectedResponse = createDefaultResponse();
        List<TaskResponse> expectedResponseList = List.of(expectedResponse);

        when(taskRepository.findByCreatedByAndStatus(any(User.class), eq(TaskStatus.PENDENTE)))
                .thenReturn(List.of(createdTask));
        when(taskMapper.toResponseList(List.of(createdTask)))
                .thenReturn(expectedResponseList);

        List<TaskResponse> result = taskService.listByStatus(TaskStatus.PENDENTE);

        assertEquals(expectedResponseList, result);
        verify(taskRepository, times(1)).findByCreatedByAndStatus(any(User.class), eq(TaskStatus.PENDENTE));
        verify(taskMapper, times(1)).toResponseList(List.of(createdTask));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver tarefas com o status especificado")
    void listByStatusCase2() {
        when(taskRepository.findByCreatedByAndStatus(any(User.class), eq(TaskStatus.CONCLUIDA)))
                .thenReturn(List.of());
        when(taskMapper.toResponseList(List.of()))
                .thenReturn(List.of());

        List<TaskResponse> result = taskService.listByStatus(TaskStatus.CONCLUIDA);

        assertEquals(List.of(), result);
        verify(taskRepository, times(1)).findByCreatedByAndStatus(any(User.class), eq(TaskStatus.CONCLUIDA));
        verify(taskMapper, times(1)).toResponseList(List.of());
    }

    @Test
    @DisplayName("Deve deletar uma tarefa com sucesso")
    void deleteCase1() {
        Task createdTask = createDefaultEntity();
        createdTask.setCreatedBy(this.authUser);

        when(taskRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(createdTask));
        doNothing().when(taskRepository).deleteById(DEFAULT_ID);

        taskService.delete(DEFAULT_ID);

        verify(taskRepository, times(1)).findById(DEFAULT_ID);
        verify(taskRepository, times(1)).deleteById(DEFAULT_ID);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar deletar tarefa inexistente")
    void deleteCase2(){
        when(taskRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.delete(NOT_FOUND_ID));
        verify(taskRepository, times(1)).findById(NOT_FOUND_ID);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao tentar deletar tarefa de outro usuário")
    void deleteCase3() {
        User differentUser = new User();
        differentUser.setId(999L);
        differentUser.setEmail("outro@dev.com");

        Task taskFromAnotherUser = createDefaultEntity();
        taskFromAnotherUser.setCreatedBy(differentUser);

        when(taskRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(taskFromAnotherUser));

        assertThrows(AccessDeniedException.class,
                () -> taskService.delete(DEFAULT_ID));
        verify(taskRepository, times(1)).findById(DEFAULT_ID);
        verify(taskRepository, never()).deleteById(any());
    }


}
