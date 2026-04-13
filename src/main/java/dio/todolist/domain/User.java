package dio.todolist.domain;

import dio.todolist.dto.UserRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Task> taskList = new ArrayList<>();


    public User(UserRequest userRequest) {
        this.name = userRequest.name();
        this.email = userRequest.email();
        this.password = userRequest.password();
    }
}
