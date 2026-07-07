package dio.todolist.repository;

import dio.todolist.domain.Task;
import dio.todolist.domain.TaskStatus;
import dio.todolist.domain.User;
import dio.todolist.dto.TaskRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    private static final Long DEFAULT_ID_NOT_FOUND = 9999L;
    private static final String DEFAULT_TITLE = "Estudar Spring Boot";
    private static final String DEFAULT_DESCRIPTION = "Estudar por 5 horas";
    private static final String DEFAULT_USER_NAME = "Felipe";
    private static final String DEFAULT_USER_EMAIL = "felipe@dev.com";
    private static final String DEFAULT_USER_PASSWORD = "@dmin123";

    @Autowired
    EntityManager entityManager;

    @Autowired
    TaskRepository taskRepository;

    private User defaultUser;

    private User createUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        this.entityManager.persist(user);
        this.entityManager.flush();
        return user;
    }

    private Task createTask(TaskRequest req) {
        return createTaskForUser(req, defaultUser);
    }

    private Task createTaskForUser(TaskRequest req, User user) {
        Task task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setCreatedBy(user);
        this.entityManager.persist(task);
        this.entityManager.flush();
        return task;
    }

    private Task createTaskWithStatus(TaskRequest req, TaskStatus status) {
        Task task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setCreatedBy(defaultUser);
        task.setStatus(status);
        this.entityManager.persist(task);
        this.entityManager.flush();
        return task;
    }

    @BeforeEach
    void setUp() {
        this.defaultUser = createUser(DEFAULT_USER_NAME, DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD);
    }

    @Test
    @DisplayName("Deve salvar uma tarefa com sucesso")
    void saveCase1() {
        Task task = createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));

        Task result = taskRepository.save(task);

        assertNotNull(result.getId());
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getDescription(), result.getDescription());
        assertNotNull(result.getCreatedBy());
        assertEquals(task.getCreatedBy().getId(), result.getCreatedBy().getId());
    }

    @Test
    @DisplayName("Deve salvar uma tarefa sem descrição")
    void saveCase2() {
        Task task = createTask(new TaskRequest(DEFAULT_TITLE, null));

        Task result = taskRepository.save(task);

        assertNotNull(result.getId());
        assertEquals(task.getTitle(), result.getTitle());
        assertNull(result.getDescription());
        assertNotNull(result.getCreatedBy());
    }

    @Test
    @DisplayName("Deve retornar uma tarefa por ID com sucesso")
    void findByIdCase1() {
        Task task = createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));

        Optional<Task> result = taskRepository.findById(task.getId());

        assertTrue(result.isPresent());
        assertEquals(task.getTitle(), result.get().getTitle());
        assertEquals(task.getCreatedBy().getId(), result.get().getCreatedBy().getId());
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar ID inexistente")
    void findByIdCase2() {
        Optional<Task> result = taskRepository.findById(DEFAULT_ID_NOT_FOUND);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve retornar uma tarefa por título e usuário com sucesso")
    void findByTitleAndCreatedByCase1() {
        Task task = createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));

        Optional<Task> result = taskRepository.findByTitleAndCreatedBy(DEFAULT_TITLE, defaultUser);

        assertTrue(result.isPresent());
        assertEquals(task.getTitle(), result.get().getTitle());
        assertEquals(task.getCreatedBy().getId(), result.get().getCreatedBy().getId());
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar por título inexistente")
    void findByTitleAndCreatedByCase2() {
        createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));

        Optional<Task> result = taskRepository.findByTitleAndCreatedBy("Título Inexistente", defaultUser);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar tarefa por usuário diferente")
    void findByTitleAndCreatedByCase3() {
        createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));
        User otherUser = createUser("Outro", "outro@email.com", "senha456");

        Optional<Task> result = taskRepository.findByTitleAndCreatedBy(DEFAULT_TITLE, otherUser);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve retornar todas as tarefas de um usuário com sucesso")
    void findByCreatedByCase1() {
        Task task1 = createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));
        Task task2 = createTask(new TaskRequest("Estudar testes unitários", null));

        Page<Task> page = taskRepository.findByCreatedBy(defaultUser, Pageable.unpaged());
        List<Task> result = page.getContent();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getTitle().equals(task1.getTitle())));
        assertTrue(result.stream().anyMatch(t -> t.getTitle().equals(task2.getTitle())));
        assertTrue(result.stream().allMatch(t -> t.getCreatedBy().getId().equals(defaultUser.getId())));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem tarefas")
    void findByCreatedByCase2() {
        User userWithoutTasks = createUser("Sem Tarefas", "notasks@email.com", "senha789");

        Page<Task> page = taskRepository.findByCreatedBy(userWithoutTasks, Pageable.unpaged());

        assertTrue(page.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar apenas tarefas do usuário especificado")
    void findByCreatedByCase3() {
        Task task = createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));
        User otherUser = createUser("Outro", "outro@email.com", "senha456");
        createTaskForUser(new TaskRequest("Tarefa do outro", "Descrição"), otherUser);

        Page<Task> page = taskRepository.findByCreatedBy(defaultUser, Pageable.unpaged());
        List<Task> result = page.getContent();

        assertEquals(1, result.size());
        assertEquals(task.getTitle(), result.getFirst().getTitle());
    }

    @Test
    @DisplayName("Deve retornar tarefas pendentes de um usuário com sucesso")
    void findByCreatedByAndStatusCase1() {
        Task pending = createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));
        createTaskWithStatus(new TaskRequest("Tarefa Concluída", "Descrição"), TaskStatus.CONCLUIDA);

        Page<Task> page = taskRepository.findByCreatedByAndStatus(defaultUser, TaskStatus.PENDENTE, Pageable.unpaged());
        List<Task> result = page.getContent();

        assertEquals(1, result.size());
        assertEquals(pending.getTitle(), result.getFirst().getTitle());
        assertEquals(TaskStatus.PENDENTE, result.getFirst().getStatus());
    }

    @Test
    @DisplayName("Deve retornar tarefas concluídas de um usuário com sucesso")
    void findByCreatedByAndStatusCase2() {
        createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));
        Task completed = createTaskWithStatus(new TaskRequest("Tarefa Concluída", "Descrição"), TaskStatus.CONCLUIDA);

        Page<Task> page = taskRepository.findByCreatedByAndStatus(defaultUser, TaskStatus.CONCLUIDA, Pageable.unpaged());
        List<Task> result = page.getContent();

        assertEquals(1, result.size());
        assertEquals(completed.getTitle(), result.getFirst().getTitle());
        assertEquals(TaskStatus.CONCLUIDA, result.getFirst().getStatus());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há tarefas com o status procurado")
    void findByCreatedByAndStatusCase3() {
        createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));

        Page<Task> page = taskRepository.findByCreatedByAndStatus(defaultUser, TaskStatus.CONCLUIDA, Pageable.unpaged());

        assertTrue(page.isEmpty());
    }

    @Test
    @DisplayName("Deve deletar uma tarefa por ID com sucesso")
    void deleteByIdCase1() {
        Task task = createTask(new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION));

        taskRepository.deleteById(task.getId());

        assertFalse(taskRepository.findById(task.getId()).isPresent());
    }

    @Test
    @DisplayName("Não deve lançar exceção ao deletar tarefa com ID inexistente")
    void deleteByIdCase2() {
        assertDoesNotThrow(() -> taskRepository.deleteById(DEFAULT_ID_NOT_FOUND));
    }
}
