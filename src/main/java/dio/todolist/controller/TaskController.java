package dio.todolist.controller;

import dio.todolist.domain.TaskStatus;
import dio.todolist.dto.TaskRequest;
import dio.todolist.dto.TaskResponse;
import dio.todolist.dto.TaskUpdate;
import dio.todolist.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@RequestBody @Valid TaskRequest taskRequest) {
        var response = taskService.create(taskRequest);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/findByTitle")
    public ResponseEntity<TaskResponse> findByTitle(@Valid @ModelAttribute TaskRequest taskRequest) {
        var response = taskService.findByTitle(taskRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listAll() {
        var response = taskService.listAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> listByStatus(@PathVariable TaskStatus status) {
        var response = taskService.listByStatus(status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                               @RequestBody @Valid TaskUpdate taskUpdate) {
        var response = taskService.update(id, taskUpdate);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
