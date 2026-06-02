package dio.todolist.service;

import dio.todolist.domain.Task;
import dio.todolist.domain.TaskStatus;
import dio.todolist.domain.User;
import dio.todolist.handler.AccessDeniedException;
import dio.todolist.handler.NotFoundException;
import dio.todolist.dto.TaskRequest;
import dio.todolist.dto.TaskResponse;
import dio.todolist.dto.TaskUpdate;
import dio.todolist.mapper.TaskMapper;
import dio.todolist.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private static final String NOT_FOUND_MESSAGE = "Tarefa não encontrada";

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    private void validateOwnership(Task task, User user) {
        if(!task.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar esta tarefa");
        }
    }

    public TaskResponse create(TaskRequest taskRequest, User authUser) {
        var entity = taskMapper.toEntity(taskRequest);
        entity.setCreatedBy(authUser);
        var response = taskRepository.save(entity);
        return taskMapper.toResponse(response);
    }

    public TaskResponse findByTitle(String title, User authUser) {
        Task task = taskRepository.findByTitleAndCreatedBy(title, authUser)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));

        return taskMapper.toResponse(task);
    }

    public Page<TaskResponse> listAll(User authUser, Pageable pageable) {
        var response = taskRepository.findByCreatedBy(authUser, pageable);
        return response.map(taskMapper::toResponse);
    }

    public TaskResponse update(Long id, TaskUpdate taskUpdate, User authUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));

        validateOwnership(task, authUser);

        taskUpdate.title().ifPresent(task::setTitle);
        taskUpdate.description().ifPresent(task::setDescription);
        taskUpdate.status().ifPresent(task::setStatus);

        Task updated = taskRepository.save(task);
        return taskMapper.toResponse(updated);
    }

    public List<TaskResponse> listByStatus(TaskStatus status, User authUser) {
        var response = taskRepository.findByCreatedByAndStatus(authUser, status);
        return taskMapper.toResponseList(response);
    }

    public void delete(Long id, User authUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MESSAGE));

        validateOwnership(task, authUser);

        taskRepository.deleteById(id);
    }

}
