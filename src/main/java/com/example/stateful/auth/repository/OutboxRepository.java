package com.example.stateful.auth.repository;

import com.example.stateful.auth.entity.OutboxMessage;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

    /**
     * Finds unprocessed messages and locks the rows so other instances of this
     * application cannot select the same messages at the same time.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM OutboxMessage m WHERE m.processed = false ORDER BY m.createdAt ASC")
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2") // "-2" represents "SKIP LOCKED" in PostgreSQL/Hibernate
    })
    List<OutboxMessage> findUnprocessedWithLock(Pageable pageable);
}
