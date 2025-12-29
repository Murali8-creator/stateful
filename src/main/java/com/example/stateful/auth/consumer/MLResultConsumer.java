package com.example.stateful.auth.consumer;

import com.example.stateful.auth.config.RabbitMqConfig;
import com.example.stateful.auth.dto.MLResultDTO;
import com.example.stateful.auth.entity.MLTask;
import com.example.stateful.auth.repository.MLTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MLResultConsumer {

    private final MLTaskRepository mlTaskRepository;

    @RabbitListener(queues = RabbitMqConfig.ML_RESULTS_QUEUE)
    public void handleMLResult(MLResultDTO resultDto) { // Spring auto-converts JSON to DTO!
        log.info(" [Java] Received result for Task ID: {}", resultDto.task_id());

        mlTaskRepository.findById(resultDto.task_id()).ifPresent(task -> {
            task.setStatus(MLTask.TaskStatus.valueOf(resultDto.status()));
            task.setResult(resultDto.result());
            mlTaskRepository.save(task);
            log.info(" [Java] Database updated successfully.");
        });
    }
}
