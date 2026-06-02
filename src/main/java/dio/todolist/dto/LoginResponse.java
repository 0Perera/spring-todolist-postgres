package dio.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "Token JWT para ser usado no header Authorization das requisições protegidas.",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.abc123")
        String token
) {}
