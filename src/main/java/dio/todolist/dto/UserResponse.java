package dio.todolist.dto;

import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email
) {}
