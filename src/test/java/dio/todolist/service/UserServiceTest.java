package dio.todolist.service;

import dio.todolist.domain.User;
import dio.todolist.dto.UserRequest;
import dio.todolist.dto.UserResponse;
import dio.todolist.dto.UserUpdate;
import dio.todolist.handler.DuplicateEmailException;
import dio.todolist.handler.NotFoundException;
import dio.todolist.mapper.UserMapper;
import dio.todolist.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final String DEFAULT_USER_EMAIL = "felipe@dev.com";
    private static final String DEFAULT_USER_NAME = "Felipe";
    private static final String DEFAULT_USER_PASSWORD = "senha123";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedpassword";
    private static final Long NOT_FOUND_ID = 9999L;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    UserMapper userMapper;

    @MockitoBean
    PasswordEncoderService passwordEncoderService;

    @Autowired
    UserService userService;

    private UserRequest createDefaultRequest() {
        return new UserRequest(DEFAULT_USER_NAME, DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD);
    }

    private User createDefaultEntity() {
        User user = new User(createDefaultRequest());
        user.setId(DEFAULT_USER_ID);
        return user;
    }

    private UserResponse createDefaultResponse() {
        return new UserResponse(DEFAULT_USER_ID, DEFAULT_USER_NAME, DEFAULT_USER_EMAIL);
    }

    private UserUpdate createDefaultUpdate() {
        return new UserUpdate(
                Optional.of("Felipe Dev"),
                Optional.of("feeps@dev.com"),
                Optional.of("novasenha123"));
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void createCase1() {
        UserRequest createdRequest = createDefaultRequest();
        User createdUser = createDefaultEntity();
        UserResponse expectedResponse = createDefaultResponse();

        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.empty());
        when(userMapper.toEntity(createdRequest)).thenReturn(createdUser);
        when(passwordEncoderService.encode(DEFAULT_USER_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);
        when(userMapper.toResponse(createdUser)).thenReturn(expectedResponse);

        UserResponse result = userService.create(createdRequest);

        assertEquals(expectedResponse, result);
        verify(userRepository, times(1)).findByEmail(DEFAULT_USER_EMAIL);
        verify(userMapper, times(1)).toEntity(createdRequest);
        verify(passwordEncoderService, times(1)).encode(DEFAULT_USER_PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toResponse(createdUser);
    }

    @Test
    @DisplayName("Deve lançar DuplicateEmailException ao criar usuário com email já cadastrado")
    void createCase2() {
        UserRequest createdRequest = createDefaultRequest();
        User existingUser = createDefaultEntity();

        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(existingUser));

        assertThrows(DuplicateEmailException.class, () -> userService.create(createdRequest));
        verify(userRepository, times(1)).findByEmail(DEFAULT_USER_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve encontrar um usuário por ID com sucesso")
    void findByIdCase1() {
        User createdUser = createDefaultEntity();
        UserResponse expectedResponse = createDefaultResponse();

        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(createdUser));
        when(userMapper.toResponse(createdUser)).thenReturn(expectedResponse);

        UserResponse result = userService.findById(DEFAULT_USER_ID);

        assertEquals(expectedResponse, result);
        verify(userRepository, times(1)).findById(DEFAULT_USER_ID);
        verify(userMapper, times(1)).toResponse(createdUser);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao buscar usuário inexistente por ID")
    void findByIdCase2() {
        when(userRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findById(NOT_FOUND_ID));
        verify(userRepository, times(1)).findById(NOT_FOUND_ID);
    }

    @Test
    @DisplayName("Deve atualizar múltiplos campos do usuário com sucesso")
    void updateCase1() {
        User createdUser = createDefaultEntity();
        UserResponse expectedResponse = createDefaultResponse();
        UserUpdate updateRequest = createDefaultUpdate();
        String newEmail = "feeps@dev.com";

        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(createdUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(passwordEncoderService.encode("novasenha123")).thenReturn("$2a$10$novasenhaencodada");
        when(userRepository.save(any(User.class))).thenReturn(createdUser);
        when(userMapper.toResponse(createdUser)).thenReturn(expectedResponse);

        UserResponse result = userService.update(DEFAULT_USER_ID, updateRequest);

        assertEquals(expectedResponse, result);
        verify(userRepository, times(1)).findById(DEFAULT_USER_ID);
        verify(userRepository, times(1)).findByEmail(newEmail);
        verify(passwordEncoderService, times(1)).encode("novasenha123");
        verify(userRepository, times(1)).save(createdUser);
        verify(userMapper, times(1)).toResponse(createdUser);
    }

    @Test
    @DisplayName("Deve atualizar senha com sucesso e garantir que foi criptografada")
    void updateCase2() {
        User createdUser = createDefaultEntity();
        UserResponse expectedResponse = createDefaultResponse();
        String newPassword = "novasenha123";
        String newEncodedPassword = "$2a$10$novasenhaencodada";
        UserUpdate updateRequest = new UserUpdate(
                Optional.empty(),
                Optional.empty(),
                Optional.of(newPassword));

        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(createdUser));
        when(passwordEncoderService.encode(newPassword)).thenReturn(newEncodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse result = userService.update(DEFAULT_USER_ID, updateRequest);

        assertEquals(expectedResponse, result);
        assertEquals(newEncodedPassword, createdUser.getPassword());
        verify(userRepository, times(1)).findById(DEFAULT_USER_ID);
        verify(passwordEncoderService, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(createdUser);
        verify(userMapper, times(1)).toResponse(createdUser);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar atualizar usuário inexistente")
    void updateCase3() {
        UserUpdate updateRequest = createDefaultUpdate();

        when(userRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(NOT_FOUND_ID, updateRequest));
        verify(userRepository, times(1)).findById(NOT_FOUND_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar DuplicateEmailException ao atualizar para email já usado por outro usuário")
    void updateCase4() {
        String conflictingEmail = "feeps@dev.com";
        UserUpdate updateRequest = createDefaultUpdate();
        User currentUser = createDefaultEntity();
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail(conflictingEmail);

        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail(conflictingEmail)).thenReturn(Optional.of(otherUser));

        assertThrows(DuplicateEmailException.class, () -> userService.update(DEFAULT_USER_ID, updateRequest));
        verify(userRepository, times(1)).findByEmail(conflictingEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve deletar um usuário com sucesso")
    void deleteCase1() {
        when(userRepository.existsById(DEFAULT_USER_ID)).thenReturn(true);
        doNothing().when(userRepository).deleteById(DEFAULT_USER_ID);

        assertDoesNotThrow(() -> userService.delete(DEFAULT_USER_ID));
        verify(userRepository, times(1)).existsById(DEFAULT_USER_ID);
        verify(userRepository, times(1)).deleteById(DEFAULT_USER_ID);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar deletar usuário inexistente")
    void deleteCase2() {
        when(userRepository.existsById(NOT_FOUND_ID)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.delete(NOT_FOUND_ID));
        verify(userRepository, times(1)).existsById(NOT_FOUND_ID);
        verify(userRepository, never()).deleteById(any());
    }
}


