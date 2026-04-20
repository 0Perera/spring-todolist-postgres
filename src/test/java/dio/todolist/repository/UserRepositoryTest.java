package dio.todolist.repository;

import dio.todolist.domain.User;
import dio.todolist.dto.UserRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.dao.DataIntegrityViolationException;

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

    private User createUser(UserRequest userRequest) {
        User user = new User(userRequest);
        this.entityManager.persist(user);
        this.entityManager.flush();
        return user;
    }
    
    @Test
    @DisplayName("Deve salvar um usuário com sucesso")
    void saveCase1() {
        // Arrange
        User createdUser = createUser(createDefaultRequest());

        // Act
        User result = userRepository.save(createdUser);

        // Assert
        assertNotNull(result.getId(), "ID do usuário deveria ser gerado");
        assertEquals(createdUser.getName(), result.getName());
        assertEquals(createdUser.getEmail(), result.getEmail());
        assertEquals(createdUser.getPassword(), result.getPassword());
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar dois usuários com o mesmo email")
    void saveCase2() {
        createUser(createDefaultRequest());

        User duplicate = new User(createDefaultRequest());

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicate);
        });
    }

    @Test
    @DisplayName("Deve salvar um usuário e atualizar dados")
    void saveCase3() {
        User createdUser = createUser(createDefaultRequest());
        
        createdUser.setName("Maria");
        createdUser.setPassword("novasenha789");
        User result = userRepository.save(createdUser);
        
        assertNotNull(result.getId(), "ID do usuário deveria ser gerado");
        assertEquals(createdUser.getName(), result.getName());
        assertEquals(createdUser.getEmail(), result.getEmail());
        assertEquals(createdUser.getPassword(), result.getPassword());
    }
    
    @Test
    @DisplayName("Deve retornar um usuário por ID com sucesso")
    void findByIdCase1() {
        User createdUser = createUser(createDefaultRequest());
        Optional<User> result = userRepository.findById(createdUser.getId());

        assertTrue(result.isPresent(), "Usuário deveria ser encontrado");
        User userFound = result.get();
        assertEquals(createdUser.getName(), userFound.getName());
        assertEquals(createdUser.getEmail(), userFound.getEmail());
        assertEquals(createdUser.getPassword(), userFound.getPassword());
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar ID inexistente")
    void findByIdCase2() {
        createUser(createDefaultRequest());
        Optional<User> result = userRepository.findById(DEFAULT_ID_NOT_FOUND);

        assertFalse(result.isPresent(), "Usuário não deveria ser encontrado");
    }
    
    @Test
    @DisplayName("Deve retornar um usuário por email com sucesso")
    void findByEmailCase1() {
        User createdUser = createUser(createDefaultRequest());
        Optional<User> result = userRepository.findByEmail(DEFAULT_USER_EMAIL);

        assertTrue(result.isPresent(), "Usuário deveria ser encontrado por email");
        User userFound = result.get();
        assertEquals(createdUser.getName(), userFound.getName());
        assertEquals(createdUser.getEmail(), userFound.getEmail());
    }

    @Test
    @DisplayName("Deve retornar vazio quando buscar email inexistente")
    void findByEmailCase2() {
        createUser(createDefaultRequest());
        Optional<User> result = userRepository.findByEmail("inexistente@email.com");

        assertFalse(result.isPresent(), "Usuário não deveria ser encontrado");
    }

    @Test
    @DisplayName("Deve retornar usuário correto quando buscar por email (múltiplos usuários)")
    void findByEmailCase3() {
        User createdUser = createUser(createDefaultRequest());
        createUser(new UserRequest("usuario 2", "usuario2@email.com", "senha555"));
        createUser(new UserRequest("Usuário 3", "usuario3@email.com", "senha666"));

        Optional<User> result = userRepository.findByEmail(createdUser.getEmail());

        assertTrue(result.isPresent(), "Usuário deveria ser encontrado");
        User userFound = result.get();
        assertEquals(createdUser.getName(), userFound.getName());
        assertEquals(createdUser.getEmail(), userFound.getEmail());
    }
    
    @Test
    @DisplayName("Deve retornar verdadeiro quando usuário existe por ID")
    void existsByIdCase1() {
        User createdUser = createUser(createDefaultRequest());
        boolean result = userRepository.existsById(createdUser.getId());

        assertTrue(result, "Usuário deveria existir");
    }

    @Test
    @DisplayName("Deve retornar falso quando usuário não existe por ID")
    void existsByIdCase2() {
        createUser(createDefaultRequest());
        boolean result = userRepository.existsById(DEFAULT_ID_NOT_FOUND);

        assertFalse(result, "Usuário não deveria existir");
    }
    
    @Test
    @DisplayName("Deve deletar um usuário por ID com sucesso")
    void deleteByIdCase1() {
        User createdUser = createUser(createDefaultRequest());
        userRepository.deleteById(createdUser.getId());

        Optional<User> result = userRepository.findById(createdUser.getId());
        assertFalse(result.isPresent(), "Usuário não deveria ser encontrado após deleção");
    }

    @Test
    @DisplayName("Não deve lançar exceção ao deletar usuário com ID inexistente")
    void deleteByIdCase2() {
        assertDoesNotThrow(() -> userRepository.deleteById(DEFAULT_ID_NOT_FOUND),
                "Não deveria lançar exceção ao deletar ID inexistente");
    }

}

