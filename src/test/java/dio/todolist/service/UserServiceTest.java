package dio.todolist.service;

import dio.todolist.domain.User;
import dio.todolist.dto.UserRequest;
import dio.todolist.dto.UserResponse;
import dio.todolist.dto.UserUpdate;
import dio.todolist.handler.AccessDeniedException;
import dio.todolist.handler.DuplicateEmailException;
import dio.todolist.handler.NotFoundException;
import dio.todolist.mapper.UserMapper;
import dio.todolist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final String DEFAULT_USER_EMAIL = "felipe@dev.com";
    private static final String DEFAULT_USER_NAME = "Felipe";
    private static final String DEFAULT_USER_PASSWORD = "senha123";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedpassword";
    private static final Long NOT_FOUND_ID = 9999L;

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    PasswordEncoderService passwordEncoderService;

    @InjectMocks
    UserService userService;

    private User authUser;

    private UserRequest createDefaultRequest() {
        return new UserRequest(DEFAULT_USER_NAME, DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD);
    }

    private User createDefaultEntity() {
        User user = new User();
        user.setId(DEFAULT_USER_ID);
        user.setName(DEFAULT_USER_NAME);
        user.setEmail(DEFAULT_USER_EMAIL);
        user.setPassword(ENCODED_PASSWORD);
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

    @BeforeEach
    void setUp() {
        authUser = new User();
        authUser.setId(DEFAULT_USER_ID);
        authUser.setEmail(DEFAULT_USER_EMAIL);
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void createCase1() {
        UserRequest request = createDefaultRequest();
        User entity = createDefaultEntity();
        entity.setPassword(DEFAULT_USER_PASSWORD); // mapper retorna senha crua, service codifica
        UserResponse expectedResponse = createDefaultResponse();

        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoderService.encode(DEFAULT_USER_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(entity);
        when(userMapper.toResponse(entity)).thenReturn(expectedResponse);

        UserResponse result = userService.create(request);

        assertEquals(expectedResponse, result);
        verify(userRepository).findByEmail(DEFAULT_USER_EMAIL);
        verify(userMapper).toEntity(request);
        verify(passwordEncoderService).encode(DEFAULT_USER_PASSWORD);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(entity);
    }

    @Test
    @DisplayName("Deve lançar DuplicateEmailException ao criar usuário com email já cadastrado")
    void createCase2() {
        UserRequest request = createDefaultRequest();
        User existing = createDefaultEntity();

        when(userRepository.findByEmail(DEFAULT_USER_EMAIL)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateEmailException.class, () -> userService.create(request));
        verify(userRepository).findByEmail(DEFAULT_USER_EMAIL);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve encontrar um usuário por ID com sucesso")
    void findByIdCase1() {
        User entity = createDefaultEntity();
        UserResponse expectedResponse = createDefaultResponse();

        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(entity));
        when(userMapper.toResponse(entity)).thenReturn(expectedResponse);

        UserResponse result = userService.findById(DEFAULT_USER_ID, authUser);

        assertEquals(expectedResponse, result);
        verify(userRepository).findById(DEFAULT_USER_ID);
        verify(userMapper).toResponse(entity);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao buscar usuário inexistente por ID")
    void findByIdCase2() {
        when(userRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findById(NOT_FOUND_ID, authUser));
        verify(userRepository).findById(NOT_FOUND_ID);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao buscar usuário de outro dono")
    void findByIdCase3() {
        User entity = createDefaultEntity();
        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(entity));

        User anotherAuth = new User();
        anotherAuth.setId(999L);

        assertThrows(AccessDeniedException.class, () -> userService.findById(DEFAULT_USER_ID, anotherAuth));
    }

    @Test
    @DisplayName("Deve atualizar múltiplos campos do usuário com sucesso")
    void updateCase1() {
        User entity = createDefaultEntity();
        UserResponse expectedResponse = createDefaultResponse();
        UserUpdate updateRequest = createDefaultUpdate();

        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(entity));
        when(userRepository.existsByEmailAndIdNot("feeps@dev.com", DEFAULT_USER_ID)).thenReturn(false);
        when(passwordEncoderService.encode("novasenha123")).thenReturn("$2a$10$novasenhaencodada");
        when(userRepository.save(any(User.class))).thenReturn(entity);
        when(userMapper.toResponse(entity)).thenReturn(expectedResponse);

        UserResponse result = userService.update(DEFAULT_USER_ID, updateRequest, authUser);

        assertEquals(expectedResponse, result);
        verify(userRepository).findById(DEFAULT_USER_ID);
        verify(userRepository).existsByEmailAndIdNot("feeps@dev.com", DEFAULT_USER_ID);
        verify(passwordEncoderService).encode("novasenha123");
        verify(userRepository).save(entity);
        verify(userMapper).toResponse(entity);
    }

    @Test
    @DisplayName("Deve atualizar senha com sucesso e garantir que foi criptografada")
    void updateCase2() {
        User entity = createDefaultEntity();
        UserResponse expectedResponse = createDefaultResponse();
        String newPassword = "novasenha123";
        String encodedNew = "$2a$10$novasenhaencodada";
        UserUpdate updateRequest = new UserUpdate(Optional.empty(), Optional.empty(), Optional.of(newPassword));

        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(entity));
        when(passwordEncoderService.encode(newPassword)).thenReturn(encodedNew);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse result = userService.update(DEFAULT_USER_ID, updateRequest, authUser);

        assertEquals(expectedResponse, result);
        assertEquals(encodedNew, entity.getPassword());
        verify(passwordEncoderService).encode(newPassword);
        verify(userRepository).save(entity);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar atualizar usuário inexistente")
    void updateCase3() {
        when(userRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(NOT_FOUND_ID, createDefaultUpdate(), authUser));
        verify(userRepository).findById(NOT_FOUND_ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar DuplicateEmailException ao atualizar para email já usado por outro usuário")
    void updateCase4() {
        User entity = createDefaultEntity();
        UserUpdate updateRequest = createDefaultUpdate();

        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(entity));
        when(userRepository.existsByEmailAndIdNot("feeps@dev.com", DEFAULT_USER_ID)).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.update(DEFAULT_USER_ID, updateRequest, authUser));
        verify(userRepository).existsByEmailAndIdNot("feeps@dev.com", DEFAULT_USER_ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar um usuário com sucesso")
    void deleteCase1() {
        when(userRepository.existsById(DEFAULT_USER_ID)).thenReturn(true);
        doNothing().when(userRepository).deleteById(DEFAULT_USER_ID);

        assertDoesNotThrow(() -> userService.delete(DEFAULT_USER_ID, authUser));
        verify(userRepository).existsById(DEFAULT_USER_ID);
        verify(userRepository).deleteById(DEFAULT_USER_ID);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar deletar usuário inexistente")
    void deleteCase2() {
        when(userRepository.existsById(NOT_FOUND_ID)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.delete(NOT_FOUND_ID, authUser));
        verify(userRepository).existsById(NOT_FOUND_ID);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao tentar atualizar usuário de outro dono")
    void updateCase5() {
        User entity = createDefaultEntity();

        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(entity));

        User anotherAuth = new User();
        anotherAuth.setId(999L);

        assertThrows(AccessDeniedException.class, () -> userService.update(DEFAULT_USER_ID, createDefaultUpdate(), anotherAuth));
        verify(userRepository).findById(DEFAULT_USER_ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao tentar deletar usuário de outro dono")
    void deleteCase3() {
        when(userRepository.existsById(DEFAULT_USER_ID)).thenReturn(true);

        User anotherAuth = new User();
        anotherAuth.setId(999L);

        assertThrows(AccessDeniedException.class, () -> userService.delete(DEFAULT_USER_ID, anotherAuth));
        verify(userRepository, never()).deleteById(any());
    }
}
