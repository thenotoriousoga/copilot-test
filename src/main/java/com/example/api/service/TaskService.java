package com.example.api.service;

import com.example.api.entity.Task;
import com.example.api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    @Transactional
    public Task create(String title, String description) {
        return taskRepository.save(new Task(title, description));
    }

    @Transactional
    public Task update(Long id, String title, String description, Task.Status status) {
        Task task = findById(id);
        task.update(title, description, status);
        return task;
    }

    @Transactional
    public void delete(Long id) {
        Task task = findById(id);
        taskRepository.delete(task);
    }
}
