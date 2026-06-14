package com.exemplo.secrest.repository;

import com.exemplo.secrest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Método customizado para buscar o usuário pelo e-mail
    Optional<User> findByEmail(String email);
}