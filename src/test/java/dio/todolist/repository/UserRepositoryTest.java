package dio.todolist.repository;

import dio.todolist.domain.User;
import dio.todolist.dto.UserRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    private static final Long DEFAULT_ID_NOT_FOUND = 9999L;
    private static final String DEFAULT_USER_NAME = "Felipe";
    private static final String DEFAULT_USER_EMAIL = "felipe@dev.com";
    private static final String DEFAULT_USER_PASSWORD = "@dmin123";

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    private UserRequest createDefaultRequest() {
        return new UserRequest(DEFAULT_USER_NAME, DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD);
    }

    private User createUser(UserRequest req) {
        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPassword(req.password());
        this.entityManager.persist(user);
        this.entityManager.flush();
        return user;
    }

    @Test
    @DisplayName("Deve salvar um usuário com sucesso")
    void saveCase1() {
        User created = createUser(createDefaultRequest());

        User result = userRepository.save(created);

        assertNotNull(result.getId());
        assertEquals(created.getName(), result.getName());
        assertEquals(created.getEmail(), result.getEmail());
        assertEquals(created.getPassword(), result.getPassword());
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar dois usuários com o mesmo email")
    void saveCase2() {
        createUser(createDefaultRequest());

        User duplicate = new User();
        duplicate.setName(DEFAULT_USER_NAME);
        duplicate.setEmail(DEFAULT_USER_EMAIL);
        duplicate.setPassword(DEFAULT_USER_PASSWORD);

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(duplicate));
    }

    @Test
    @DisplayName("Deve salvar um usuário e atualizar dados")
    void saveCase3() {
        User created = createUser(createDefaultRequest());

        created.setName("Maria");
        created.setPassword("novasenha789");
        User result = userRepository.save(created);

        assertEquals("Maria", result.getName());
        assertEquals("novasenha789", result.getPassword());
        assertEquals(DEFAULT_USER_EMAIL, result.getEmail());
    }

    @Test
    @DisplayName("Deve retornar um usuário por ID com sucesso")
    void findByIdCase1() {
        User created = createUser(createDefaultRequest());

        Optional<User> result = userRepository.findById(created.getId());

        assertTrue(result.isPresent());
        assertEquals(created.getName(), result.get().getName());
        assertEquals(created.getEmail(), result.get().getEmail());
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar ID inexistente")
    void findByIdCase2() {
        Optional<User> result = userRepository.findById(DEFAULT_ID_NOT_FOUND);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve retornar um usuário por email com sucesso")
    void findByEmailCase1() {
        User created = createUser(createDefaultRequest());

        Optional<User> result = userRepository.findByEmail(DEFAULT_USER_EMAIL);

        assertTrue(result.isPresent());
        assertEquals(created.getName(), result.get().getName());
        assertEquals(created.getEmail(), result.get().getEmail());
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar email inexistente")
    void findByEmailCase2() {
        createUser(createDefaultRequest());

        Optional<User> result = userRepository.findByEmail("inexistente@email.com");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve retornar usuário correto quando há múltiplos usuários")
    void findByEmailCase3() {
        User created = createUser(createDefaultRequest());
        createUser(new UserRequest("Usuário 2", "usuario2@email.com", "senha555"));
        createUser(new UserRequest("Usuário 3", "usuario3@email.com", "senha666"));

        Optional<User> result = userRepository.findByEmail(DEFAULT_USER_EMAIL);

        assertTrue(result.isPresent());
        assertEquals(created.getName(), result.get().getName());
        assertEquals(DEFAULT_USER_EMAIL, result.get().getEmail());
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando usuário existe por ID")
    void existsByIdCase1() {
        User created = createUser(createDefaultRequest());

        assertTrue(userRepository.existsById(created.getId()));
    }

    @Test
    @DisplayName("Deve retornar falso quando usuário não existe por ID")
    void existsByIdCase2() {
        assertFalse(userRepository.existsById(DEFAULT_ID_NOT_FOUND));
    }

    @Test
    @DisplayName("Deve deletar um usuário por ID com sucesso")
    void deleteByIdCase1() {
        User created = createUser(createDefaultRequest());

        userRepository.deleteById(created.getId());

        assertFalse(userRepository.findById(created.getId()).isPresent());
    }

    @Test
    @DisplayName("Não deve lançar exceção ao deletar usuário com ID inexistente")
    void deleteByIdCase2() {
        assertDoesNotThrow(() -> userRepository.deleteById(DEFAULT_ID_NOT_FOUND));
    }
}
