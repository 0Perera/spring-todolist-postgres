package dio.todolist.config;

import dio.todolist.domain.User;
import dio.todolist.handler.InvalidTokenException;
import dio.todolist.service.JwtService;
import dio.todolist.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    JwtService jwtService;

    @Mock
    UserDetailsServiceImpl userDetailsService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @InjectMocks
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve deixar a requisição passar quando não houver header Authorization")
    void doFilterInternalCase1() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve deixar a requisição passar quando Authorization não começar com Bearer")
    void doFilterInternalCase2() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve autenticar o usuário e passar a requisição quando token for válido")
    void doFilterInternalCase3() throws Exception {
        User user = new User();
        user.setEmail("felipe@dev.com");
        String validToken = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("felipe@dev.com");
        when(userDetailsService.loadUserByUsername("felipe@dev.com")).thenReturn(user);
        when(jwtService.validateToken(validToken, user)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).validateToken(validToken, user);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando token for inválido")
    void doFilterInternalCase4() throws Exception {
        String invalidToken = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtService.extractUsername(invalidToken))
                .thenThrow(new InvalidTokenException("Token inválido ou expirado"));
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando usuário não for encontrado")
    void doFilterInternalCase5() throws Exception {
        String token = "valid.format.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("inexistente@dev.com");
        when(userDetailsService.loadUserByUsername("inexistente@dev.com"))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuário não encontrado"));
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }
}
