package com.example.stateful.auth.controller;

import com.example.stateful.auth.entity.MLTask;
import com.example.stateful.auth.repository.MLTaskRepository;
import com.example.stateful.auth.service.MLTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ml")
@RequiredArgsConstructor
public class MLTaskController {
    private final MLTaskService mlTaskService;
    private final MLTaskRepository mlTaskRepository;

    @PostMapping("/process")
    public ResponseEntity<String> startTask(@RequestParam String data) throws Exception {
        String taskId = mlTaskService.submitTask(data);
        return ResponseEntity.accepted().body("Task started with ID: " + taskId);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<MLTask> getStatus(@PathVariable String id) {
        return ResponseEntity.of(mlTaskRepository.findById(id));
    }
}
