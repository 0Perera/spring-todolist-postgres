package dio.todolist.service;

import dio.todolist.domain.Task;
import dio.todolist.domain.TaskStatus;
import dio.todolist.domain.User;
import dio.todolist.dto.TaskRequest;
import dio.todolist.dto.TaskResponse;
import dio.todolist.dto.TaskUpdate;
import dio.todolist.handler.AccessDeniedException;
import dio.todolist.handler.NotFoundException;
import dio.todolist.mapper.TaskMapper;
import dio.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final String DEFAULT_USER_EMAIL = "felipe@dev.com";
    private static final Long NOT_FOUND_ID = 9999L;
    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_TITLE = "Estudar Spring Boot";
    private static final String DEFAULT_DESCRIPTION = "Estudar por 5 horas";

    @Mock
    TaskRepository taskRepository;

    @Mock
    TaskMapper taskMapper;

    @InjectMocks
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
        Task task = new Task();
        task.setId(DEFAULT_ID);
        task.setTitle(DEFAULT_TITLE);
        task.setDescription(DEFAULT_DESCRIPTION);
        task.setStatus(TaskStatus.PENDENTE);
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
        authUser = new User();
        authUser.setId(DEFAULT_USER_ID);
        authUser.setEmail(DEFAULT_USER_EMAIL);
    }

    @Test
    @DisplayName("Deve criar uma tarefa com sucesso")
    void createCase1() {
        TaskRequest request = createDefaultRequest();
        Task entity = createDefaultEntity();
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskMapper.toEntity(request)).thenReturn(entity);
        when(taskRepository.save(any(Task.class))).thenReturn(entity);
        when(taskMapper.toResponse(entity)).thenReturn(expectedResponse);

        TaskResponse result = taskService.create(request, authUser);

        assertEquals(expectedResponse, result);
        verify(taskMapper).toEntity(request);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toResponse(entity);
    }

    @Test
    @DisplayName("Deve criar uma tarefa sem descrição")
    void createCase2() {
        TaskRequest request = new TaskRequest(DEFAULT_TITLE, null);
        Task entity = createDefaultEntity();
        entity.setDescription(null);
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskMapper.toEntity(request)).thenReturn(entity);
        when(taskRepository.save(entity)).thenReturn(entity);
        when(taskMapper.toResponse(entity)).thenReturn(expectedResponse);

        TaskResponse result = taskService.create(request, authUser);

        assertEquals(expectedResponse, result);
        verify(taskMapper).toEntity(request);
        verify(taskRepository).save(entity);
        verify(taskMapper).toResponse(entity);
    }

    @Test
    @DisplayName("Deve encontrar uma tarefa por título com sucesso")
    void findByTitleCase1() {
        Task entity = createDefaultEntity();
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskRepository.findByTitleAndCreatedBy(DEFAULT_TITLE, authUser)).thenReturn(Optional.of(entity));
        when(taskMapper.toResponse(entity)).thenReturn(expectedResponse);

        TaskResponse result = taskService.findByTitle(DEFAULT_TITLE, authUser);

        assertEquals(expectedResponse, result);
        verify(taskRepository).findByTitleAndCreatedBy(DEFAULT_TITLE, authUser);
        verify(taskMapper).toResponse(entity);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando não encontrar tarefa por título")
    void findByTitleCase2() {
        when(taskRepository.findByTitleAndCreatedBy(DEFAULT_TITLE, authUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.findByTitle(DEFAULT_TITLE, authUser));
        verify(taskRepository).findByTitleAndCreatedBy(DEFAULT_TITLE, authUser);
    }

    @Test
    @DisplayName("Deve listar todas as tarefas do usuário autenticado")
    void listAllCase1() {
        Task entity = createDefaultEntity();
        TaskResponse expectedResponse = createDefaultResponse();
        Page<Task> taskPage = new PageImpl<>(List.of(entity));

        when(taskRepository.findByCreatedBy(eq(authUser), any(Pageable.class))).thenReturn(taskPage);
        when(taskMapper.toResponse(entity)).thenReturn(expectedResponse);

        Page<TaskResponse> result = taskService.listAll(authUser, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResponse, result.getContent().get(0));
        verify(taskRepository).findByCreatedBy(eq(authUser), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tiver tarefas")
    void listAllCase2() {
        when(taskRepository.findByCreatedBy(eq(authUser), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        Page<TaskResponse> result = taskService.listAll(authUser, Pageable.unpaged());

        assertTrue(result.isEmpty());
        verify(taskRepository).findByCreatedBy(eq(authUser), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve atualizar apenas os campos presentes (description e status) e ignorar Optional.empty()")
    void updateCase1() {
        TaskUpdate updateRequest = createDefaultUpdate(); // title=empty, description="Estudar por 3 horas", status=CONCLUIDA
        Task entity = createDefaultEntity(); // title=DEFAULT_TITLE, status=PENDENTE
        entity.setCreatedBy(authUser);
        TaskResponse expectedResponse = createDefaultResponse();

        when(taskRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(entity));
        when(taskRepository.save(entity)).thenReturn(entity);
        when(taskMapper.toResponse(entity)).thenReturn(expectedResponse);

        TaskResponse result = taskService.update(DEFAULT_ID, updateRequest, authUser);

        assertEquals(DEFAULT_TITLE, entity.getTitle()); // Optional.empty() — não deve ter mudado
        assertEquals("Estudar por 3 horas", entity.getDescription()); // deve ter sido aplicado
        assertEquals(TaskStatus.CONCLUIDA, entity.getStatus()); // deve ter sido aplicado
        assertEquals(expectedResponse, result);
        verify(taskRepository).findById(DEFAULT_ID);
        verify(taskRepository).save(entity);
        verify(taskMapper).toResponse(entity);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar atualizar tarefa inexistente")
    void updateCase2() {
        when(taskRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.update(NOT_FOUND_ID, createDefaultUpdate(), authUser));
        verify(taskRepository).findById(NOT_FOUND_ID);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao tentar atualizar tarefa de outro usuário")
    void updateCase3() {
        User differentUser = new User();
        differentUser.setId(999L);
        Task task = createDefaultEntity();
        task.setCreatedBy(differentUser);

        when(taskRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.update(DEFAULT_ID, createDefaultUpdate(), authUser));
        verify(taskRepository).findById(DEFAULT_ID);
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar tarefas por status com sucesso")
    void listByStatusCase1() {
        Task entity = createDefaultEntity();
        TaskResponse expectedResponse = createDefaultResponse();
        Page<Task> taskPage = new PageImpl<>(List.of(entity));

        when(taskRepository.findByCreatedByAndStatus(eq(authUser), eq(TaskStatus.PENDENTE), any(Pageable.class))).thenReturn(taskPage);
        when(taskMapper.toResponse(entity)).thenReturn(expectedResponse);

        Page<TaskResponse> result = taskService.listByStatus(TaskStatus.PENDENTE, authUser, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResponse, result.getContent().get(0));
        verify(taskRepository).findByCreatedByAndStatus(eq(authUser), eq(TaskStatus.PENDENTE), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver tarefas com o status especificado")
    void listByStatusCase2() {
        when(taskRepository.findByCreatedByAndStatus(eq(authUser), eq(TaskStatus.CONCLUIDA), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        Page<TaskResponse> result = taskService.listByStatus(TaskStatus.CONCLUIDA, authUser, Pageable.unpaged());

        assertTrue(result.isEmpty());
        verify(taskRepository).findByCreatedByAndStatus(eq(authUser), eq(TaskStatus.CONCLUIDA), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve deletar uma tarefa com sucesso")
    void deleteCase1() {
        Task entity = createDefaultEntity();
        entity.setCreatedBy(authUser);

        when(taskRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(entity));
        doNothing().when(taskRepository).deleteById(DEFAULT_ID);

        taskService.delete(DEFAULT_ID, authUser);

        verify(taskRepository).findById(DEFAULT_ID);
        verify(taskRepository).deleteById(DEFAULT_ID);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar deletar tarefa inexistente")
    void deleteCase2() {
        when(taskRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.delete(NOT_FOUND_ID, authUser));
        verify(taskRepository).findById(NOT_FOUND_ID);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao tentar deletar tarefa de outro usuário")
    void deleteCase3() {
        User differentUser = new User();
        differentUser.setId(999L);
        Task task = createDefaultEntity();
        task.setCreatedBy(differentUser);

        when(taskRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.delete(DEFAULT_ID, authUser));
        verify(taskRepository).findById(DEFAULT_ID);
        verify(taskRepository, never()).deleteById(any());
    }
}
