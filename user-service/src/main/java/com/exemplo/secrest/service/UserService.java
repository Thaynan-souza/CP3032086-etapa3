package com.exemplo.secrest.service;

import com.exemplo.secrest.dto.CreateUserDto;
import com.exemplo.secrest.dto.LoginUserDto;
import com.exemplo.secrest.dto.RecoveryJwtTokenDto;
import com.exemplo.secrest.dto.UpdateProfileDto;
import com.exemplo.secrest.dto.UserProfileDto;
import com.exemplo.secrest.entity.Role;
import com.exemplo.secrest.entity.User;
import com.exemplo.secrest.repository.UserRepository;
import com.exemplo.secrest.security.service.JwtTokenService;
import com.exemplo.secrest.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RecoveryJwtTokenDto authenticateUser(LoginUserDto loginDto) {
        var authToken = new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password());
        Authentication authentication = authenticationManager.authenticate(authToken);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtTokenService.generateToken(userDetails);
        return new RecoveryJwtTokenDto(token);
    }

    public void createUser(CreateUserDto createDto) {
        User newUser = User.builder()
                .name(createDto.name())
                .email(createDto.email())
                .password(passwordEncoder.encode(createDto.password()))
                .roles(List.of(Role.builder().name(createDto.role()).build()))
                .build();
        userRepository.save(newUser);
    }
    
    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        return new UserProfileDto(user.getId(), user.getEmail(), roles);
    }

    // ==========================================
    // MÉTODO ETAPA 4 (Corrigido e blindado)
    // ==========================================
    @Transactional
    public User updateProfile(String email, UpdateProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        user.setName(dto.name());

        // Usa uma lista mutável para o Hibernate não quebrar!
        List<Role> novasRoles = new ArrayList<>();
        novasRoles.add(Role.builder().name(dto.role()).build());
        user.setRoles(novasRoles);

        return userRepository.save(user);
    }
}