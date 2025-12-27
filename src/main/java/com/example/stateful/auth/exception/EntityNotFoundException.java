package com.example.stateful.auth.exception;

public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException(String mesage){
        super(mesage);
    }
}
