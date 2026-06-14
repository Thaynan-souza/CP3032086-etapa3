package com.exemplo.secrest.controller;

import com.exemplo.secrest.dto.EmailDto;
import com.exemplo.secrest.dto.RecoveryJwtTokenDto;
import com.exemplo.secrest.entity.Role;
import com.exemplo.secrest.entity.User;
import com.exemplo.secrest.enums.RoleName;
import com.exemplo.secrest.producer.UserProducer;
import com.exemplo.secrest.repository.UserRepository;
import com.exemplo.secrest.security.service.JwtTokenService;
import com.exemplo.secrest.security.service.UserDetailsImpl;
import com.exemplo.secrest.service.CodigoCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CodigoCacheService codigoCacheService;

    @Autowired
    private UserProducer userProducer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    // Rota 1: Pede o código
    @PostMapping("/request-code")
    public ResponseEntity<Void> requestCode(@RequestBody RequestCodeDto dto) {
        String email = dto.email();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .roles(List.of(Role.builder().name(RoleName.ROLE_CUSTOMER).build()))
                    .build();
            return userRepository.save(newUser);
        });

        String code = String.format("%06d", new Random().nextInt(999999));
        codigoCacheService.saveCode(email, code);

        EmailDto emailDto = new EmailDto(UUID.randomUUID(), email, "Seu código de acesso", "Aqui está o seu código de acesso: " + code);
        userProducer.publishMessageEmail(emailDto);

        return ResponseEntity.ok().build();
    }

    // Rota 2: Verifica o código e devolve o Token
    @PostMapping("/verify-code")
    public ResponseEntity<RecoveryJwtTokenDto> verifyCode(@RequestBody VerifyCodeDto dto) {
        boolean isValid = codigoCacheService.isValidCode(dto.email(), dto.code());
        
        if (!isValid) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized
        }

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String token = jwtTokenService.generateToken(userDetails);

        return ResponseEntity.ok(new RecoveryJwtTokenDto(token));
    }

    // DTOs Internos
    record RequestCodeDto(String email) {}
    record VerifyCodeDto(String email, String code) {}
}