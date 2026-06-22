package com.exemplo.secrest.controller;

import com.exemplo.secrest.dto.CreateUserDto;
import com.exemplo.secrest.dto.LoginUserDto;
import com.exemplo.secrest.dto.RecoveryJwtTokenDto;
import com.exemplo.secrest.dto.UpdateProfileDto; // <-- Novo DTO importado
import com.exemplo.secrest.dto.UserProfileDto;
import com.exemplo.secrest.entity.User; // <-- Entidade User importada
import com.exemplo.secrest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody CreateUserDto dto) {
        userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<RecoveryJwtTokenDto> login(@RequestBody LoginUserDto dto) {
        RecoveryJwtTokenDto token = userService.authenticateUser(dto);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Autenticado com sucesso!");
    }

    @GetMapping("/test/customer")
    public ResponseEntity<String> customerTest() {
        return ResponseEntity.ok("Acesso de CUSTOMER autorizado!");
    }

    @GetMapping("/test/administrator")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Acesso de ADMINISTRATOR autorizado!");
    }
    
    // Rota do Desafio Prático
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDto userProfile = userService.getUserProfile(email);
        return ResponseEntity.ok(userProfile);
    }

    // ==========================================
    // ROTA DA ETAPA 4: Atualizar Perfil
    // ==========================================
    @PostMapping("/update-profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            Authentication authentication, 
            @RequestBody UpdateProfileDto dto) {
        
        // 1. Extrai o e-mail de quem está logado diretamente do Token JWT
        String email = authentication.getName();
        
        // 2. Chama o serviço para atualizar o nome e o cargo no banco
        User updated = userService.updateProfile(email, dto);
        
        // 3. Usa o método do seu Desafio Prático para converter e retornar o DTO
        UserProfileDto userProfile = userService.getUserProfile(updated.getEmail());
        
        return ResponseEntity.ok(userProfile);
    }
}