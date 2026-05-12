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
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoderService passwordEncoderService;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoderService passwordEncoderService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoderService = passwordEncoderService;
    }

    public UserResponse create(UserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.email()).isPresent()) {
            throw new DuplicateEmailException("Email já está em uso");
        }
        var entity = userMapper.toEntity(userRequest);
        entity.setPassword(passwordEncoderService.encode(entity.getPassword()));
        var response = userRepository.save(entity);
        return userMapper.toResponse(response);
    }

    public UserResponse findById(Long id, User authUser) {
        var response = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario não encontrado"));
        if(!id.equals(authUser.getId())) {
            throw new AccessDeniedException("Você não tem permissão para consultar esse usuário");
        }
        return userMapper.toResponse(response);
    }

    public UserResponse update(Long id, UserUpdate userUpdate, User authUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario não encontrado"));

        if(!id.equals(authUser.getId())) {
            throw new AccessDeniedException("Você não tem permissão para atualizar este usuário");
        }

        userUpdate.name().ifPresent(user::setName);
        userUpdate.email().ifPresent(newEmail -> {
            if (userRepository.existsByEmailAndIdNot(newEmail, id)) {
                throw new DuplicateEmailException("Email já esta em uso por outro usuário");
            }
            user.setEmail(newEmail);
        });
        userUpdate.password().ifPresent(password -> user.setPassword(passwordEncoderService.encode(password)));

        User updated = userRepository.save(user);
        return userMapper.toResponse(updated);
    }

    public void delete(Long id, User authUser) {
        if(!userRepository.existsById(id)){
            throw new NotFoundException("Usuario não encontrado");
        }
        if(!id.equals(authUser.getId())) {
            throw new AccessDeniedException("Você não tem permissão para deletar este usuário");
        }

        userRepository.deleteById(id);
    }

}
