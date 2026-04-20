package dio.todolist.controller;

import dio.todolist.dto.UserRequest;
import dio.todolist.dto.UserResponse;
import dio.todolist.dto.UserUpdate;
import dio.todolist.handler.DuplicateEmailException;
import dio.todolist.handler.NotFoundException;
import dio.todolist.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

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

    private UserRequest createDefaultRequest() {
        return new UserRequest(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }

    private UserResponse createDefaultResponse() {
        return new UserResponse(
                DEFAULT_ID,
                DEFAULT_NAME,
                DEFAULT_EMAIL
        );
    }

    @Test
    @DisplayName("Deve retornar 201 Created ao criar um usuário com dados válidos")
    void createCase1() throws Exception {
        UserRequest createdRequest = createDefaultRequest();
        UserResponse expectedResponse = createDefaultResponse();
        String userJson = objectMapper.writeValueAsString(createdRequest);

        when(userService.create(createdRequest)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao criar um usuário com dados inválidos")
    void createCase2() throws Exception {
        UserRequest createdRequest = new UserRequest("", "", "");
        String userJson = objectMapper.writeValueAsString(createdRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print()
                );
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao criar um usuário com email já cadastrado")
    void createCase3() throws Exception {
        UserRequest createdRequest = createDefaultRequest();
        String userJson = objectMapper.writeValueAsString(createdRequest);

        when(userService.create(createdRequest)).thenThrow(new DuplicateEmailException("Email já está em uso"));

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andDo(MockMvcResultHandlers.print());
    }
    
    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao buscar um usuário por ID existente")
    void findByIdCase1() throws Exception {
        UserResponse expectedResponse = createDefaultResponse();

        when(userService.findById(DEFAULT_ID)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + DEFAULT_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 Not Found ao buscar um usuário por ID inexistente")
    void findByIdCase2() throws Exception {
        when(userService.findById(DEFAULT_ID)).thenThrow(new NotFoundException("Usuário não encontrado"));

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + DEFAULT_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao atualizar um usuário com dados válidos")
    void updateCase1() throws Exception {
        UserUpdate updateRequest = new UserUpdate(
                Optional.of("Felipe Souza"),
                Optional.of("felipe.souza@dev.com"),
                Optional.of("novaSenha123")
        );
        UserResponse expectedResponse = new UserResponse(
                DEFAULT_ID,
                "Felipe Souza",
                "felipe.souza@dev.com"
        );
        String userJson = objectMapper.writeValueAsString(updateRequest);

        when(userService.update(DEFAULT_ID, updateRequest)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + DEFAULT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 OK ao atualizar apenas o nome de um usuário")
    void updateCase2() throws Exception {
        UserUpdate updateRequest = new UserUpdate(
                Optional.of("Felipe Souza"),
                Optional.empty(),
                Optional.empty()
        );
        UserResponse expectedResponse = new UserResponse(
                DEFAULT_ID,
                "Felipe Souza",
                DEFAULT_EMAIL
        );
        String userJson = objectMapper.writeValueAsString(updateRequest);

        when(userService.update(DEFAULT_ID, updateRequest)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + DEFAULT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 Not Found ao atualizar um usuário inexistente")
    void updateCase3() throws Exception {
        UserUpdate updateRequest = new UserUpdate(
                Optional.of("Felipe Souza"),
                Optional.of("felipe.souza@dev.com"),
                Optional.of("novaSenha123")
        );
        String userJson = objectMapper.writeValueAsString(updateRequest);

        when(userService.update(DEFAULT_ID, updateRequest)).thenThrow(new NotFoundException("Usuário não encontrado"));

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + DEFAULT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 409 Conflict ao atualizar para email já usado por outro usuário")
    void updateCase4() throws Exception {
        UserUpdate updateRequest = new UserUpdate(
                Optional.empty(),
                Optional.of(DEFAULT_EMAIL),
                Optional.empty()
        );
        String userJson = objectMapper.writeValueAsString(updateRequest);

        when(userService.update(DEFAULT_ID, updateRequest)).thenThrow(new DuplicateEmailException("Email já está em uso"));

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + DEFAULT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 204 No Content ao deletar um usuário existente")
    void deleteCase1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 Not Found ao deletar um usuário inexistente")
    void deleteCase2() throws Exception {
        doThrow(new NotFoundException("Usuário não encontrado")).when(userService).delete(DEFAULT_ID);

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + DEFAULT_ID))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

}

