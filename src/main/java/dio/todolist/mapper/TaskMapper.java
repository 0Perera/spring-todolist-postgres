package dio.todolist.mapper;

import dio.todolist.dto.TaskRequest;
import dio.todolist.dto.TaskResponse;
import dio.todolist.domain.Task;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Task toEntity(@Valid TaskRequest taskRequest);

    TaskResponse toResponse(Task taskEntity);

    List<TaskResponse> toResponseList(List<Task> taskEntities);
}
