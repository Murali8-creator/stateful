package com.example.stateful.auth.service;

import com.example.stateful.auth.entity.MLTask;
import com.example.stateful.auth.entity.OutboxMessage;
import com.example.stateful.auth.repository.MLTaskRepository;
import com.example.stateful.auth.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MLTaskService {

    private final MLTaskRepository mlTaskRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public String submitTask(String data) throws JsonProcessingException {

        // Insert the record into ml_tasks table as PENDING
        MLTask task = new MLTask();
        task.setInputData(data);
        task.setStatus(MLTask.TaskStatus.PENDING);
        mlTaskRepository.save(task);

        // 3. Create the payload for Python
        Map<String, String> payload = Map.of(
                "task_id", task.getId(),
                "data", data
        );

        // 4. Save to Outbox
        OutboxMessage outbox = new OutboxMessage();
        outbox.setEventType("ML_PROCESS_START");
        outbox.setPayload(objectMapper.writeValueAsString(payload));
        outboxRepository.save(outbox);

        return task.getId();

        // Because of @Transactional, if saving the outbox fails,
        // the MLTask record is rolled back. No "ghost" tasks!
    }
}
