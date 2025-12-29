package com.example.stateful.auth.repository;

import com.example.stateful.auth.entity.MLTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MLTaskRepository extends JpaRepository<MLTask, String> {}