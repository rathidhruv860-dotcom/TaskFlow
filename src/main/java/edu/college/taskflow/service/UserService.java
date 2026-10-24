package edu.college.taskflow.service;

import edu.college.taskflow.dto.UserResponse;
import edu.college.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) { this.users = users; }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return users.findAllByOrderByNameAsc().stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt()))
                .toList();
    }
}
