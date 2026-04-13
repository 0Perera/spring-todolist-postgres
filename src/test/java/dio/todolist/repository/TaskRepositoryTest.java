package dio.todolist.repository;

import dio.todolist.domain.TaskStatus;
import dio.todolist.domain.User;
import dio.todolist.dto.TaskRequest;
import dio.todolist.domain.Task;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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

    private TaskRequest createDefaultRequest() {
        return new TaskRequest(DEFAULT_TITLE, DEFAULT_DESCRIPTION);
    }

    private User createUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        this.entityManager.persist(user);
        this.entityManager.flush();
        return user;
    }

    private Task createTask(TaskRequest taskRequest){
        Task task = new Task(taskRequest);
        task.setCreatedBy(defaultUser);
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
        // Arrange
        Task createdTask = this.createTask(createDefaultRequest());

        // Act
        Task result = taskRepository.save(createdTask);

        // Assert
        assertNotNull(createdTask.getId(), "ID da tarefa deveria ser gerado");
        assertEquals(createdTask.getTitle(), result.getTitle());
        assertEquals(createdTask.getDescription(), result.getDescription());
        assertEquals(createdTask.getDateCreated(), result.getDateCreated());
        assertEquals(createdTask.getStatus(), result.getStatus());
        assertNotNull(result.getCreatedBy(), "Tarefa deveria ter um usuário associado");
        assertEquals(createdTask.getCreatedBy().getId(), result.getCreatedBy().getId());
    }

    @Test
    @DisplayName("Deve salvar uma tarefa sem descrição")
    void saveCase2() {
        Task createdTask = this.createTask(createDefaultRequest());
        createdTask.setDescription(null);

        Task result = taskRepository.save(createdTask);

        assertNotNull(result.getId(), "ID da tarefa deveria ser gerado");
        assertEquals(createdTask.getTitle(), result.getTitle());
        assertNull(result.getDescription());
        assertEquals(createdTask.getDateCreated(), result.getDateCreated());
        assertNotNull(result.getCreatedBy(), "Tarefa deveria ter um usuário associado");
        assertEquals(createdTask.getCreatedBy().getId(), result.getCreatedBy().getId());
    }

    @Test
    @DisplayName("Deve retornar uma tarefa por ID com sucesso")
    void findByIdCase1() {
        Task createdTask = this.createTask(createDefaultRequest());

        Optional<Task> result = taskRepository.findById(createdTask.getId());

        assertTrue(result.isPresent(), "Tarefa deveria ser encontrada");
        Task taskFound = result.get();
        assertEquals(createdTask.getTitle(), taskFound.getTitle());
        assertEquals(createdTask.getDescription(), taskFound.getDescription());
        assertEquals(createdTask.getDateCreated(), taskFound.getDateCreated());
        assertNotNull(taskFound.getCreatedBy(), "Tarefa deveria ter um usuário associado");
        assertEquals(createdTask.getCreatedBy().getId(), taskFound.getCreatedBy().getId());
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar ID inexistente")
    void findByIdCase2() {
        createTask(createDefaultRequest());

        Optional<Task> result = taskRepository.findById(DEFAULT_ID_NOT_FOUND);

        assertFalse(result.isPresent(), "Tarefa não deveria ser encontrada");
    }

    @Test
    @DisplayName("Deve retornar uma tarefa por título e usuário com sucesso")
    void findByTitleAndCreatedByCase1() {
        Task createdTask = this.createTask(createDefaultRequest());

        Optional<Task> result = taskRepository.findByTitleAndCreatedBy(DEFAULT_TITLE, defaultUser);

        assertTrue(result.isPresent(), "Tarefa deveria ser encontrada");
        Task taskFound = result.get();
        assertEquals(createdTask.getTitle(), taskFound.getTitle());
        assertEquals(createdTask.getDescription(), taskFound.getDescription());
        assertNotNull(taskFound.getCreatedBy(), "Tarefa deveria ter um usuário associado");
        assertEquals(createdTask.getCreatedBy().getId(), taskFound.getCreatedBy().getId());
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar por título inexistente")
    void findByTitleAndCreatedByCase2() {
        createTask(createDefaultRequest());

        Optional<Task> result = taskRepository.findByTitleAndCreatedBy("Título Inexistente", defaultUser);

        assertFalse(result.isPresent(), "Tarefa não deveria ser encontrada");
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar por usuário diferente")
    void findByTitleAndCreatedByCase3() {
        this.createTask(createDefaultRequest());
        User otherUser = createUser("Outro Usuário", "outro@email.com", "senha456");

        Optional<Task> result = taskRepository.findByTitleAndCreatedBy(DEFAULT_TITLE, otherUser);

        assertFalse(result.isPresent(), "Tarefa não deveria ser encontrada para outro usuário");
    }

    @Test
    @DisplayName("Deve retornar todas as tarefas de um usuário com sucesso")
    void findByCreatedByCase1() {
        Task createdTask1 = this.createTask(createDefaultRequest());
        Task createdTask2 = this.createTask(new TaskRequest("Estudar testes unitários", null));

        List<Task> result = taskRepository.findByCreatedBy(defaultUser);

        assertEquals(2, result.size(), "Deveria retornar 2 tarefas do usuário");
        assertTrue(result.stream().anyMatch(task -> task.getTitle().equals(createdTask1.getTitle())));
        assertTrue(result.stream().anyMatch(task -> task.getTitle().equals(createdTask2.getTitle())));
        assertTrue(result.stream().allMatch(task -> task.getCreatedBy().getId().equals(createdTask1.getCreatedBy().getId())));
        assertTrue(result.stream().allMatch(task -> task.getCreatedBy().getId().equals(createdTask2.getCreatedBy().getId())));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem tarefas")
    void findByCreatedByCase2() {
        User userWithoutTasks = createUser("Usuário Sem Tarefas", "notasks@email.com", "senha789");

        List<Task> result = taskRepository.findByCreatedBy(userWithoutTasks);

        assertTrue(result.isEmpty(), "Deveria retornar lista vazia para usuário sem tarefas");
    }

    @Test
    @DisplayName("Deve retornar apenas tarefas do usuário especificado")
    void findByCreatedByCase3() {
        Task taskUser1 = createTask(createDefaultRequest());
        
        User otherUser = createUser("Outro Usuário", "outro@email.com", "senha456");
        
        Task taskUser2 = createTask(new TaskRequest("Tarefa do outro usuário", "Descrição"));
        taskUser2.setCreatedBy(otherUser);
        this.entityManager.persist(taskUser2);
        this.entityManager.flush();

        List<Task> result = taskRepository.findByCreatedBy(defaultUser);

        assertEquals(1, result.size(), "Deveria retornar apenas 1 tarefa do usuário");
        assertEquals(taskUser1.getTitle(), result.get(0).getTitle());
    }

    @Test
    @DisplayName("Deve retornar tarefas pendentes de um usuário com sucesso")
    void findByCreatedByAndStatusCase1() {
        Task taskPending = createTask(createDefaultRequest());
        Task taskCompleted = createTask(new TaskRequest("Tarefa Concluída", "Descrição"));
        taskCompleted.setStatus(TaskStatus.CONCLUIDA);
        this.entityManager.persist(taskCompleted);
        this.entityManager.flush();

        List<Task> result = taskRepository.findByCreatedByAndStatus(defaultUser, TaskStatus.PENDENTE);

        assertEquals(1, result.size(), "Deveria retornar apenas tarefas pendentes");
        assertEquals(taskPending.getTitle(), result.get(0).getTitle());
        assertEquals(TaskStatus.PENDENTE, result.get(0).getStatus());
    }

    @Test
    @DisplayName("Deve retornar tarefas concluídas de um usuário com sucesso")
    void findByCreatedByAndStatusCase2() {
        this.createTask(createDefaultRequest());
        Task taskCompleted = this.createTask(new TaskRequest("Tarefa Concluída", "Descrição"));
        taskCompleted.setStatus(TaskStatus.CONCLUIDA);
        this.entityManager.persist(taskCompleted);
        this.entityManager.flush();

        List<Task> result = taskRepository.findByCreatedByAndStatus(defaultUser, TaskStatus.CONCLUIDA);

        assertEquals(1, result.size(), "Deveria retornar apenas tarefas concluídas");
        assertEquals(taskCompleted.getTitle(), result.get(0).getTitle());
        assertEquals(TaskStatus.CONCLUIDA, result.get(0).getStatus());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há tarefas com o status procurado")
    void findByCreatedByAndStatusCase3() {
        this.createTask(createDefaultRequest());

        List<Task> result = taskRepository.findByCreatedByAndStatus(defaultUser, TaskStatus.CONCLUIDA);

        assertTrue(result.isEmpty(), "Deveria retornar lista vazia quando nenhuma tarefa tem o status");
    }

    @Test
    @DisplayName("Deve deletar uma tarefa por ID com sucesso")
    void deleteByIdCase1() {
        Task createdTask = this.createTask(createDefaultRequest());

        taskRepository.deleteById(createdTask.getId());

        Optional<Task> result = taskRepository.findById(createdTask.getId());
        assertFalse(result.isPresent(), "Tarefa não deveria ser encontrada");
    }

    @Test
    @DisplayName("Não deve lançar exceção ao deletar tarefa com ID inexistente")
    void deleteByIdCase2() {
        assertDoesNotThrow(() -> taskRepository.deleteById(DEFAULT_ID_NOT_FOUND),
                "Não deveria lançar exceção ao deletar ID inexistente");
    }



}
