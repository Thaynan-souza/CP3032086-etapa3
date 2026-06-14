package com.exemplo.secrest.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodigoCacheService {
    
    private final Map<String, CodeData> cache = new ConcurrentHashMap<>();

    public void saveCode(String email, String code) {
        cache.put(email, new CodeData(code, LocalDateTime.now().plusMinutes(5)));
    }

    // NOVO MÉTODO: Valida o código
    public boolean isValidCode(String email, String code) {
        CodeData data = cache.get(email);
        
        if (data != null && data.code().equals(code) && data.expirationTime().isAfter(LocalDateTime.now())) {
            cache.remove(email); // Limpa da memória após sucesso
            return true;
        }
        return false;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanUp() {
        cache.entrySet().removeIf(entry -> entry.getValue().expirationTime().isBefore(LocalDateTime.now()));
    }

    record CodeData(String code, LocalDateTime expirationTime) {}
}