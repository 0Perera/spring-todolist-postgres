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
import dio.todolist.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.userRepository = userRepository;
    }

    private User authUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario não encontrada: " + email));
    }

    private void validateOwnership(Task task, User user) {
        if(!task.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar esta tarefa");
        }
    }

    public TaskResponse create(TaskRequest taskRequest) {
        var entity = taskMapper.toEntity(taskRequest);
        entity.setCreatedBy(authUser());
        var response = taskRepository.save(entity);
        return taskMapper.toResponse(response);
    }

    public TaskResponse findByTitle(TaskRequest taskRequest) {
        User user = authUser();
        Task task = taskRepository.findByTitleAndCreatedBy(taskRequest.title(), user)
                .orElseThrow(() -> new NotFoundException("Tarefa não encontrada"));

        return taskMapper.toResponse(task);
    }

    public List<TaskResponse> listAll() {
        var response = taskRepository.findByCreatedBy(authUser());
        return taskMapper.toResponseList(response);
    }

    public TaskResponse update(Long id, TaskUpdate taskUpdate) {
        User user = authUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarefa não encontrada"));

        validateOwnership(task, user);

        taskUpdate.title().ifPresent(task::setTitle);
        taskUpdate.description().ifPresent(task::setDescription);
        taskUpdate.status().ifPresent(task::setStatus);

        Task updated = taskRepository.save(task);
        return taskMapper.toResponse(updated);
    }

    public List<TaskResponse> listByStatus(TaskStatus status) {
        var response = taskRepository.findByCreatedByAndStatus(authUser(), status);
        return taskMapper.toResponseList(response);
    }

    public void delete(Long id) {
        User user = authUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarefa não encontrada"));

        validateOwnership(task, user);

        taskRepository.deleteById(id);
    }

}
