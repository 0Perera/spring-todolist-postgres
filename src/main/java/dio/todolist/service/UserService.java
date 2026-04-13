package dio.todolist.service;

import dio.todolist.dto.UserRequest;
import dio.todolist.dto.UserResponse;
import dio.todolist.dto.UserUpdate;
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
        var entity = userMapper.toEntity(userRequest);
        entity.setPassword(passwordEncoderService.encode(entity.getPassword()));
        var response = userRepository.save(entity);
        return userMapper.toResponse(response);
    }

    public UserResponse findById(Long id) {
        var response = userRepository.findById(id);
        return response.map(userMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Usuario não encontrado"));
    }

    public UserResponse update(Long id, UserUpdate userUpdate) {
        return userRepository.findById(id)
                .map(existing -> {
                    userUpdate.name().ifPresent(existing::setName);
                    userUpdate.email().ifPresent(existing::setEmail);
                    userUpdate.password().ifPresent(password ->
                            existing.setPassword(passwordEncoderService.encode(password))
                    );

                    return userRepository.save(existing);
                })
                .map(userMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Usuario não encontrado"));
    }

    public void delete(Long id) {
        if(!userRepository.existsById(id)){
            throw new NotFoundException("Usuario não encontrado");
        }
        userRepository.deleteById(id);
    }

}
