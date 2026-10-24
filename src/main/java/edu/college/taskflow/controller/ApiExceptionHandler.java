package edu.college.taskflow.controller;

import edu.college.taskflow.service.ResourceNotFoundException;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> invalid(MethodArgumentNotValidException e){
        Map<String,String> fields=new LinkedHashMap<>(); e.getBindingResult().getFieldErrors().forEach(x->fields.putIfAbsent(x.getField(),x.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("timestamp",Instant.now(),"status",400,"error","Validation failed","fields",fields));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> conflict(IllegalArgumentException e){return error(HttpStatus.CONFLICT,e.getMessage());}
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<?> notFound(ResourceNotFoundException e){return error(HttpStatus.NOT_FOUND,e.getMessage());}
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<?> unauthorized(BadCredentialsException e){return error(HttpStatus.UNAUTHORIZED,e.getMessage());}
    private ResponseEntity<?> error(HttpStatus status,String message){return ResponseEntity.status(status).body(Map.of("timestamp",Instant.now(),"status",status.value(),"error",message));}
}
