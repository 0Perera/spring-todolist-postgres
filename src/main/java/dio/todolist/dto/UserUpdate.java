package dio.todolist.dto;

import java.util.Optional;

public record UserUpdate(
        Optional<String> name,
        Optional<String> email,
        Optional<String> password
) {}
