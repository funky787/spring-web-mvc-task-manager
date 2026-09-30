package com.example.taskmanager.service;

import com.example.taskmanager.config.AppProperties;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;
    private final AppProperties properties;
    private final AtomicLong idGenerator = new AtomicLong(0);

    public TaskServiceImpl(TaskRepository repository, AppProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    public List<Task> getAll(String status, String priority) {
        Stream<Task> stream = repository.findAll().stream();

        if (status != null && !status.isBlank()) {
            Task.Status parsedStatus = parseStatus(status);
            stream = stream.filter(task -> task.getStatus() == parsedStatus);
        }

        if (priority != null && !priority.isBlank()) {
            Task.Priority parsedPriority = parsePriority(priority);
            stream = stream.filter(task -> task.getPriority() == parsedPriority);
        }

        return stream.toList();
    }

    @Override
    public Optional<Task> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Task create(Task task) {
        validateTask(task);

        if (repository.findAll().size() >= properties.getMaxTasks()) {
            throw new IllegalArgumentException(
                    "Достигнут лимит задач: " + properties.getMaxTasks());
        }

        task.setId(idGenerator.incrementAndGet());

        if (task.getPriority() == null) {
            task.setPriority(properties.getDefaultPriority());
        }

        if (task.getStatus() == null) {
            task.setStatus(Task.Status.NEW);
        }

        return repository.save(task);
    }

    @Override
    public Optional<Task> update(Long id, Task task) {
        Optional<Task> existing = repository.findById(id);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        validateTask(task);

        Task oldTask = existing.get();

        task.setId(id);

        if (task.getPriority() == null) {
            task.setPriority(properties.getDefaultPriority());
        }

        if (task.getStatus() == null) {
            task.setStatus(oldTask.getStatus());
        }

        repository.save(task);
        return Optional.of(task);
    }

    @Override
    public Task updateStatus(Long id, Task.Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Поле status обязательно");
        }

        Task task = repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setStatus(status);
        return repository.save(task);
    }

    @Override
    public void delete(Long id) {
        if (!repository.delete(id)) {
            throw new TaskNotFoundException(id);
        }
    }

    @Override
    public Map<String, Long> getStats() {
        Map<String, Long> result = new LinkedHashMap<>();

        Arrays.stream(Task.Status.values())
                .forEach(status -> result.put(
                        status.name(),
                        repository.findAll().stream()
                                .filter(task -> task.getStatus() == status)
                                .count()
                ));

        return result;
    }

    private void validateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Тело запроса не передано");
        }

        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Название задачи не может быть пустым");
        }
    }

    private Task.Status parseStatus(String status) {
        try {
            return Task.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Неизвестный status: " + status +
                            ". Допустимо: NEW, IN_PROGRESS, DONE");
        }
    }

    private Task.Priority parsePriority(String priority) {
        try {
            return Task.Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Неизвестный priority: " + priority +
                            ". Допустимо: LOW, MEDIUM, HIGH");
        }
    }
}
