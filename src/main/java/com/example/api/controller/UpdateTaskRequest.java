package com.example.api.controller;

import com.example.api.entity.Task;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateTaskRequest {

    private String title;
    private String description;
    private Task.Status status;
}
