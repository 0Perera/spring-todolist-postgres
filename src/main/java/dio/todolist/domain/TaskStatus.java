package dio.todolist.domain;

public enum TaskStatus {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
