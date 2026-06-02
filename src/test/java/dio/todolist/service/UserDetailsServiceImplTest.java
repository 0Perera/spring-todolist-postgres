package dio.todolist.service;

import dio.todolist.domain.User;
import dio.todolist.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    private static final String DEFAULT_USER_EMAIL = "felipe@dev.com";
    private static final String DEFAULT_USER_PASSWORD = "$2a$10$encodedpassword";
    private static final String DEFAULT_USER_NAME = "Felipe";

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    private User createDefaultUser() {
        return createUserWith(DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD);
    }

    private User createUserWith(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(DEFAULT_USER_NAME);
        return user;
    }

    @Test
    @DisplayName("Deve carregar usuário por email com sucesso")
    void loadUserByUsernameCase1() {
        User entity = createDefaultUser();
        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(entity));

        UserDetails result = userDetailsService.loadUserByUsername(DEFAULT_USER_EMAIL);

        assertNotNull(result);
        assertEquals(entity.getEmail(), result.getUsername());
        assertEquals(entity.getPassword(), result.getPassword());
        verify(userRepository).findByEmail(DEFAULT_USER_EMAIL);
    }

    @Test
    @DisplayName("Deve retornar lista de autoridades vazia (sem roles definidas)")
    void loadUserByUsernameCase2() {
        User entity = createDefaultUser();
        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(entity));

        UserDetails result = userDetailsService.loadUserByUsername(DEFAULT_USER_EMAIL);

        assertNotNull(result);
        assertTrue(result.getAuthorities().isEmpty());
        verify(userRepository).findByEmail(DEFAULT_USER_EMAIL);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não existir")
    void loadUserByUsernameCase3() {
        String nonExistentEmail = "inexistente@dev.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(nonExistentEmail));
        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Deve validar que UserDetails contém dados corretos do usuário")
    void loadUserByUsernameCase4() {
        User entity = createDefaultUser();
        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(entity));

        UserDetails result = userDetailsService.loadUserByUsername(DEFAULT_USER_EMAIL);

        assertNotNull(result);
        assertEquals(entity.getEmail(), result.getUsername());
        assertEquals(entity.getPassword(), result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        verify(userRepository).findByEmail(DEFAULT_USER_EMAIL);
    }

    @Test
    @DisplayName("Deve carregar múltiplos usuários diferentes com sucesso")
    void loadUserByUsernameCase5() {
        String user2Email = "outro@dev.com";
        String user2Password = "$2a$10$outrasenha";

        User user1 = createDefaultUser();
        User user2 = createUserWith(user2Email, user2Password);

        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(user1));
        when(userRepository.findByEmail(user2Email)).thenReturn(Optional.of(user2));

        UserDetails result1 = userDetailsService.loadUserByUsername(DEFAULT_USER_EMAIL);
        UserDetails result2 = userDetailsService.loadUserByUsername(user2Email);

        assertEquals(user1.getEmail(), result1.getUsername());
        assertEquals(user2.getEmail(), result2.getUsername());
        assertNotEquals(result1.getPassword(), result2.getPassword());
        verify(userRepository).findByEmail(DEFAULT_USER_EMAIL);
        verify(userRepository).findByEmail(user2Email);
    }
}
