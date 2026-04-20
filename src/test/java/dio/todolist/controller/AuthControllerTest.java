package dio.todolist.controller;

import dio.todolist.config.SecurityConfig;
import dio.todolist.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String DEFAULT_USERNAME = "felipe@dev.com";
    private static final String DEFAULT_PASSWORD = "@dmin123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    private LoginRequest createDefaultLoginRequest() {
        return new LoginRequest(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao realizar login com credenciais válidas")
    void loginCase1() throws Exception {
        LoginRequest createdRequest = createDefaultLoginRequest();
        String loginJson = objectMapper.writeValueAsString(createdRequest);

        Authentication auth = new UsernamePasswordAuthenticationToken(createdRequest.email(), createdRequest.password());
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Login realizado")))
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized ao realizar login com credenciais inválidas")
    void loginCase2() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("errado@dev.com", "senhaIncorreta");
        String loginJson = objectMapper.writeValueAsString(invalidRequest);
        
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Email ou Senha invalidos"));
        
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Email ou Senha invalidos"))
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao realizar logout")
    void logoutCase1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout realizado!"))
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar o email do usuário autenticado no endpoint /me")
    @WithMockUser(username = DEFAULT_USERNAME)
    void meCase1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/me")
                    .with(SecurityMockMvcRequestPostProcessors.user(DEFAULT_USERNAME)))
                .andExpect(status().isOk())
                .andExpect(content().string(DEFAULT_USERNAME))
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve informar que não há usuário autenticado no endpoint /me")
    void meCase2() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("Nenhum usuário autenticado"))
                .andDo(MockMvcResultHandlers.print()
                );
    }
}