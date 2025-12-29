package com.example.stateful.auth.service;

import com.example.stateful.auth.entity.OutboxMessage;
import com.example.stateful.auth.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MLTaskService {
    private final OutboxRepository outboxRepository;

    @Transactional
    public void submitTask(String inputData) {
        OutboxMessage outbox = new OutboxMessage();
        outbox.setEventType("ML_PROCESS_START");
        outbox.setPayload(inputData);
        outboxRepository.save(outbox);
    }
}
