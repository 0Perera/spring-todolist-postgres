package dio.todolist.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordEncoderServiceTest {

    private static final String RAW_PASSWORD = "senha123";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedpassword";

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    PasswordEncoderService passwordEncoderService;

    @Test
    @DisplayName("Deve encriptar uma senha com sucesso")
    void encodeCase1() {
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        String result = passwordEncoderService.encode(RAW_PASSWORD);

        assertEquals(ENCODED_PASSWORD, result);
        verify(passwordEncoder).encode(RAW_PASSWORD);
    }

    @Test
    @DisplayName("Deve retornar diferentes hashes para mesma senha")
    void encodeCase2() {
        when(passwordEncoder.encode(RAW_PASSWORD))
                .thenReturn("$2a$10$encodedpassword1")
                .thenReturn("$2a$10$encodedpassword2");

        String result1 = passwordEncoderService.encode(RAW_PASSWORD);
        String result2 = passwordEncoderService.encode(RAW_PASSWORD);

        assertNotEquals(result1, result2);
        verify(passwordEncoder, times(2)).encode(RAW_PASSWORD);
    }

    @Test
    @DisplayName("Deve validar corretamente uma senha que corresponde ao hash")
    void matchesCase1() {
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        boolean result = passwordEncoderService.matches(RAW_PASSWORD, ENCODED_PASSWORD);

        assertTrue(result);
        verify(passwordEncoder).matches(RAW_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Deve rejeitar uma senha que não corresponde ao hash")
    void matchesCase2() {
        when(passwordEncoder.matches("senhaErrada", ENCODED_PASSWORD)).thenReturn(false);

        boolean result = passwordEncoderService.matches("senhaErrada", ENCODED_PASSWORD);

        assertFalse(result);
        verify(passwordEncoder).matches("senhaErrada", ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Deve validar múltiplas tentativas de senha")
    void matchesCase3() {
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches("senhaErrada1", ENCODED_PASSWORD)).thenReturn(false);
        when(passwordEncoder.matches("senhaErrada2", ENCODED_PASSWORD)).thenReturn(false);

        assertTrue(passwordEncoderService.matches(RAW_PASSWORD, ENCODED_PASSWORD));
        assertFalse(passwordEncoderService.matches("senhaErrada1", ENCODED_PASSWORD));
        assertFalse(passwordEncoderService.matches("senhaErrada2", ENCODED_PASSWORD));
        verify(passwordEncoder, times(3)).matches(anyString(), eq(ENCODED_PASSWORD));
    }
}
