package edu.college.taskflow.controller;

import edu.college.taskflow.dto.*;
import edu.college.taskflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth){this.auth=auth;}
    @PostMapping("/register") @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public AuthResponse register(@Valid @RequestBody RegisterRequest r){return auth.register(r);}
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest r){return auth.login(r);}
}
