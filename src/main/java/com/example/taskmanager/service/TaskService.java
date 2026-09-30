package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskService {

    List<Task> getAll(String status, String priority);

    Optional<Task> findById(Long id);

    Task create(Task task);

    Optional<Task> update(Long id, Task task);

    Task updateStatus(Long id, Task.Status status);

    void delete(Long id);

    Map<String, Long> getStats();
}
