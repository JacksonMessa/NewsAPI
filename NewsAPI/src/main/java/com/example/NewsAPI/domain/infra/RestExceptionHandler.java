package com.example.NewsAPI.domain.infra;

import com.example.NewsAPI.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    private ResponseEntity<String> runTimeExceptionHandler(){
        return ResponseEntity.status(500).body("An unexpected error occurred.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    private ResponseEntity<String> methodArgumentTypeMismatchException(){
        return ResponseEntity.status(400).body("Some of the parameters sent contain the wrong type.");
    }

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    private ResponseEntity<String> userAlreadyRegisteredExceptionHandler(){
        return ResponseEntity.status(400).body("This username is already registered.");
    }

    @ExceptionHandler(IncorrectLoginCredentialsException.class)
    private ResponseEntity<String> incorrectLoginCredentialsExceptionHandler(){
        return ResponseEntity.status(401).body("Incorrect username or password.");
    }

    @ExceptionHandler(DateConvertException.class)
    private ResponseEntity<String> dateConvertExceptionHandler(){
        return ResponseEntity.status(400).body("Error converting data. This parameter must be sent in DD/MM/YYYY format.");
    }

    @ExceptionHandler(NewsNotFoundException.class)
    private ResponseEntity<String> newsNotFoundExceptionHandler(){
        return ResponseEntity.status(404).body("No news item was found with the provided ID.");
    }

    @ExceptionHandler(BelongsToAnotherWriterException.class)
    private ResponseEntity<String> belongsToAnotherWriterExceptionHandler(){
        return ResponseEntity.status(401).body("You are not authorized to update this news because it belongs to another user.");
    }
}
