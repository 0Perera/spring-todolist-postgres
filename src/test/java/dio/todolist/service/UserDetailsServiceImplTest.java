package dio.todolist.service;

import dio.todolist.domain.User;
import dio.todolist.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserDetailsServiceImplTest {

    private static final String DEFAULT_USER_EMAIL = "felipe@dev.com";
    private static final String DEFAULT_USER_PASSWORD = "$2a$10$encodedpassword";
    private static final String DEFAULT_USER_NAME = "Felipe";

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    private User createDefaultUser() {
        User user = new User();
        user.setEmail(DEFAULT_USER_EMAIL);
        user.setPassword(DEFAULT_USER_PASSWORD);
        user.setName(DEFAULT_USER_NAME);
        return user;
    }

    @Test
    @DisplayName("Deve carregar usuário por email com sucesso")
    void loadUserByUsernameCase1() {
        User createdUser = createDefaultUser();

        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(createdUser));

        UserDetails result = userDetailsService.loadUserByUsername(DEFAULT_USER_EMAIL);

        assertNotNull(result);
        assertEquals(createdUser.getEmail(), result.getUsername());
        assertEquals(createdUser.getPassword(), result.getPassword());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        verify(userRepository, times(1)).findByEmail(DEFAULT_USER_EMAIL);
    }

    @Test
    @DisplayName("Deve validar que o usuário tem a autoridade ROLE_USER")
    void loadUserByUsernameCase2() {
        User createdUser = createDefaultUser();

        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(createdUser));

        UserDetails result = userDetailsService.loadUserByUsername(DEFAULT_USER_EMAIL);

        assertNotNull(result);
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(userRepository, times(1)).findByEmail(DEFAULT_USER_EMAIL);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não existir")
    void loadUserByUsernameCase3() {
        String nonExistentEmail = "inexistente@dev.com";

        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(nonExistentEmail));
        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Deve validar que UserDetails contém dados corretos do usuário")
    void loadUserByUsernameCase4() {
        User createdUser = createDefaultUser();

        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(createdUser));

        UserDetails result = userDetailsService.loadUserByUsername(DEFAULT_USER_EMAIL);

        assertNotNull(result);
        assertEquals(createdUser.getEmail(), result.getUsername());
        assertEquals(createdUser.getPassword(), result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        verify(userRepository, times(1)).findByEmail(DEFAULT_USER_EMAIL);
    }

    @Test
    @DisplayName("Deve carregar múltiplos usuários diferentes com sucesso")
    void loadUserByUsernameCase5() {

        User createdUser1 = createDefaultUser();
        User createdUser2 = new User();
        String user2Email = "outro@dev.com";
        createdUser2.setEmail(user2Email);
        String user2Password = "$2a$10$outrasenha";
        createdUser2.setPassword(user2Password);

        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(createdUser1));
        when(userRepository.findByEmail(user2Email)).thenReturn(Optional.of(createdUser2));

        UserDetails result1 = userDetailsService.loadUserByUsername(DEFAULT_USER_EMAIL);
        UserDetails result2 = userDetailsService.loadUserByUsername(user2Email);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(createdUser1.getEmail(), result1.getUsername());
        assertEquals(createdUser2.getEmail(), result2.getUsername());
        assertNotEquals(result1.getPassword(), result2.getPassword());
        verify(userRepository, times(1)).findByEmail(DEFAULT_USER_EMAIL);
        verify(userRepository, times(1)).findByEmail(user2Email);
    }
}

