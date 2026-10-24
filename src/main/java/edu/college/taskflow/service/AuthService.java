package edu.college.taskflow.service;

import edu.college.taskflow.dto.*;
import edu.college.taskflow.model.AppUser;
import edu.college.taskflow.repository.UserRepository;
import edu.college.taskflow.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
    public AuthService(UserRepository users,PasswordEncoder encoder,JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
    public AuthResponse register(RegisterRequest r){
        String email=r.email().trim().toLowerCase();
        if(users.existsByEmail(email)) throw new IllegalArgumentException("An account with this email already exists.");
        AppUser user=users.save(new AppUser(r.name().trim(),email,encoder.encode(r.password()))); return response(user);
    }
    public AuthResponse login(LoginRequest r){
        AppUser user=users.findByEmail(r.email().trim().toLowerCase()).orElseThrow(()->new BadCredentialsException("Invalid email or password."));
        if(!encoder.matches(r.password(),user.getPasswordHash())) throw new BadCredentialsException("Invalid email or password.");
        return response(user);
    }
    private AuthResponse response(AppUser u){return new AuthResponse(jwt.generate(u.getEmail()),"Bearer",jwt.getExpirationSeconds(),new UserResponse(u.getId(),u.getName(),u.getEmail(),u.getCreatedAt()));}
}
