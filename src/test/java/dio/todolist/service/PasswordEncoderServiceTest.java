package dio.todolist.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class PasswordEncoderServiceTest {

    private static final String RAW_PASSWORD = "senha123";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedpassword";

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @Autowired
    PasswordEncoderService passwordEncoderService;

    @Test
    @DisplayName("Deve encriptar uma senha com sucesso")
    void encodeCase1() {
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        String result = passwordEncoderService.encode(RAW_PASSWORD);

        assertEquals(ENCODED_PASSWORD, result);
        verify(passwordEncoder, times(1)).encode(RAW_PASSWORD);
    }

    @Test
    @DisplayName("Deve retornar diferentes hashes para mesma senha")
    void encodeCase2() {
        String encodedPassword1 = "$2a$10$encodedpassword1";
        String encodedPassword2 = "$2a$10$encodedpassword2";

        when(passwordEncoder.encode(RAW_PASSWORD))
                .thenReturn(encodedPassword1)
                .thenReturn(encodedPassword2);

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
        verify(passwordEncoder, times(1)).matches(RAW_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Deve rejeitar uma senha que não corresponde ao hash")
    void matchesCase2() {
        String wrongPassword = "senhaErrada";

        when(passwordEncoder.matches(wrongPassword, ENCODED_PASSWORD)).thenReturn(false);

        boolean result = passwordEncoderService.matches(wrongPassword, ENCODED_PASSWORD);

        assertFalse(result);
        verify(passwordEncoder, times(1)).matches(wrongPassword, ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Deve validar múltiplas tentativas de senha")
    void matchesCase3() {
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches("senhaErrada1", ENCODED_PASSWORD)).thenReturn(false);
        when(passwordEncoder.matches("senhaErrada2", ENCODED_PASSWORD)).thenReturn(false);

        boolean result1 = passwordEncoderService.matches(RAW_PASSWORD, ENCODED_PASSWORD);
        boolean result2 = passwordEncoderService.matches("senhaErrada1", ENCODED_PASSWORD);
        boolean result3 = passwordEncoderService.matches("senhaErrada2", ENCODED_PASSWORD);

        assertTrue(result1);
        assertFalse(result2);
        assertFalse(result3);
        verify(passwordEncoder, times(3)).matches(anyString(), eq(ENCODED_PASSWORD));
    }
}

