package dio.todolist.service;

import dio.todolist.domain.User;
import dio.todolist.handler.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String JWT_SECRET = "VGhpcyBpcyBhIHRlc3QgSldUIHNlY3JldCBrZXkgZm9yIHVuaXQgdGVzdHM=";
    private static final long EXPIRATION = 86400000L;
    private static final String USER_EMAIL = "felipe@dev.com";

    @InjectMocks
    JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationSign", EXPIRATION);

        user = new User();
        user.setId(1L);
        user.setEmail(USER_EMAIL);
    }

    @Test
    @DisplayName("Deve gerar um token JWT não nulo para um usuário válido")
    void generateTokenCase1() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve extrair o email correto do token gerado")
    void extractUsernameCase1() {
        String token = jwtService.generateToken(user);

        String extracted = jwtService.extractUsername(token);

        assertEquals(USER_EMAIL, extracted);
    }

    @Test
    @DisplayName("Deve validar com sucesso um token válido para o usuário correto")
    void validateTokenCase1() {
        String token = jwtService.generateToken(user);

        boolean result = jwtService.validateToken(token, user);

        assertTrue(result);
    }

    @Test
    @DisplayName("Deve lançar InvalidTokenException para token adulterado")
    void validateTokenCase2() {
        String invalidToken = "token.invalido.adulterado";

        assertThrows(InvalidTokenException.class, () -> jwtService.validateToken(invalidToken, user));
    }

    @Test
    @DisplayName("Deve lançar InvalidTokenException para token expirado")
    void validateTokenCase3() {
        ReflectionTestUtils.setField(jwtService, "expirationSign", -1000L);
        String expiredToken = jwtService.generateToken(user);
        ReflectionTestUtils.setField(jwtService, "expirationSign", EXPIRATION);

        assertThrows(InvalidTokenException.class, () -> jwtService.validateToken(expiredToken, user));
    }

    @Test
    @DisplayName("Deve retornar false quando token pertence a outro usuário")
    void validateTokenCase4() {
        String token = jwtService.generateToken(user);

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("outro@dev.com");

        boolean result = jwtService.validateToken(token, otherUser);

        assertFalse(result);
    }
}
