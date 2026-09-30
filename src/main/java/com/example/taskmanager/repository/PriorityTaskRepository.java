package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("prod")
public class PriorityTaskRepository implements TaskRepository {

    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();

    @Override
    public List<Task> findAll() {
        return tasks.values().stream()
                .sorted(Comparator.comparingInt(this::priorityOrder))
                .toList();
    }

    private int priorityOrder(Task task) {
        if (task.getPriority() == null) {
            return 3;
        }

        return switch (task.getPriority()) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public Task save(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public boolean delete(Long id) {
        return tasks.remove(id) != null;
    }
}
