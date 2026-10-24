package edu.college.taskflow.controller;

import edu.college.taskflow.dto.UserResponse;
import edu.college.taskflow.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users = users; }
    @GetMapping public List<UserResponse> listUsers() { return users.listUsers(); }
}
