package dio.todolist.controller;

import dio.todolist.dto.UserRequest;
import dio.todolist.dto.UserResponse;
import dio.todolist.dto.UserUpdate;
import dio.todolist.handler.AccessDeniedException;
import dio.todolist.handler.DuplicateEmailException;
import dio.todolist.handler.NotFoundException;
import dio.todolist.service.JwtService;
import dio.todolist.service.UserDetailsServiceImpl;
import dio.todolist.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_NAME = "Felipe";
    private static final String DEFAULT_EMAIL = "felipe@dev.com";
    private static final String DEFAULT_PASSWORD = "@dmin123";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    private UserRequest createDefaultRequest() {
        return new UserRequest(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }

    private UserResponse createDefaultResponse() {
        return new UserResponse(DEFAULT_ID, DEFAULT_NAME, DEFAULT_EMAIL);
    }

    @Test
    @DisplayName("Deve retornar 201 Created ao criar um usuário com dados válidos")
    void createCase1() throws Exception {
        UserRequest request = createDefaultRequest();
        UserResponse expectedResponse = createDefaultResponse();
        String userJson = objectMapper.writeValueAsString(request);

        when(userService.create(request)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao criar um usuário com dados inválidos")
    void createCase2() throws Exception {
        String userJson = objectMapper.writeValueAsString(new UserRequest("", "", ""));

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao criar usuário com email já cadastrado")
    void createCase3() throws Exception {
        UserRequest request = createDefaultRequest();
        String userJson = objectMapper.writeValueAsString(request);

        when(userService.create(request)).thenThrow(new DuplicateEmailException("Email já está em uso"));

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao buscar usuário por ID existente")
    void findByIdCase1() throws Exception {
        when(userService.findById(eq(DEFAULT_ID), any())).thenReturn(createDefaultResponse());

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 403 Forbidden ao tentar acessar dados de outro usuário")
    void findByIdCase3() throws Exception {
        when(userService.findById(eq(DEFAULT_ID), any())).thenThrow(new AccessDeniedException("Sem permissão"));

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 Not Found ao buscar usuário por ID inexistente")
    void findByIdCase2() throws Exception {
        when(userService.findById(eq(DEFAULT_ID), any())).thenThrow(new NotFoundException("Usuário não encontrado"));

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao atualizar usuário com dados válidos")
    void updateCase1() throws Exception {
        UserUpdate updateRequest = new UserUpdate(
                Optional.of("Felipe Souza"),
                Optional.of("felipe.souza@dev.com"),
                Optional.of("novaSenha123"));
        UserResponse expectedResponse = new UserResponse(DEFAULT_ID, "Felipe Souza", "felipe.souza@dev.com");
        String userJson = objectMapper.writeValueAsString(updateRequest);

        when(userService.update(eq(DEFAULT_ID), any(UserUpdate.class), any())).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao atualizar apenas o nome do usuário")
    void updateCase2() throws Exception {
        UserUpdate updateRequest = new UserUpdate(Optional.of("Felipe Souza"), Optional.empty(), Optional.empty());
        UserResponse expectedResponse = new UserResponse(DEFAULT_ID, "Felipe Souza", DEFAULT_EMAIL);
        String userJson = objectMapper.writeValueAsString(updateRequest);

        when(userService.update(eq(DEFAULT_ID), any(UserUpdate.class), any())).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 Not Found ao atualizar usuário inexistente")
    void updateCase3() throws Exception {
        UserUpdate updateRequest = new UserUpdate(Optional.of("Felipe Souza"), Optional.of("felipe.souza@dev.com"), Optional.of("novaSenha123"));
        String userJson = objectMapper.writeValueAsString(updateRequest);

        when(userService.update(eq(DEFAULT_ID), any(UserUpdate.class), any())).thenThrow(new NotFoundException("Usuário não encontrado"));

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 403 Forbidden ao tentar atualizar dados de outro usuário")
    void updateCase5() throws Exception {
        UserUpdate updateRequest = new UserUpdate(Optional.of("Felipe Souza"), Optional.empty(), Optional.empty());
        String userJson = objectMapper.writeValueAsString(updateRequest);

        when(userService.update(eq(DEFAULT_ID), any(UserUpdate.class), any())).thenThrow(new AccessDeniedException("Sem permissão"));

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 409 Conflict ao atualizar para email já usado por outro usuário")
    void updateCase4() throws Exception {
        UserUpdate updateRequest = new UserUpdate(Optional.empty(), Optional.of(DEFAULT_EMAIL), Optional.empty());
        String userJson = objectMapper.writeValueAsString(updateRequest);

        when(userService.update(eq(DEFAULT_ID), any(UserUpdate.class), any())).thenThrow(new DuplicateEmailException("Email já está em uso"));

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + DEFAULT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 204 No Content ao deletar usuário existente")
    void deleteCase1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 403 Forbidden ao tentar deletar conta de outro usuário")
    void deleteCase3() throws Exception {
        doThrow(new AccessDeniedException("Sem permissão")).when(userService).delete(eq(DEFAULT_ID), any());

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 Not Found ao deletar usuário inexistente")
    void deleteCase2() throws Exception {
        doThrow(new NotFoundException("Usuário não encontrado")).when(userService).delete(eq(DEFAULT_ID), any());

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }
}
