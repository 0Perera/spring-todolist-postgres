package dio.todolist.mapper;

import dio.todolist.domain.User;
import dio.todolist.dto.TaskResponse;
import dio.todolist.dto.UserRequest;
import dio.todolist.dto.UserResponse;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(@Valid UserRequest  userRequest);

    UserResponse toResponse(User userEntity);

    List<UserResponse> toResponseList(List<User> userEntities);

}
