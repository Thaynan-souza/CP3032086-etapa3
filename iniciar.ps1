# Script para iniciar o Ecossistema Secrest
Write-Host "Iniciando os microsservicos e o frontend..." -ForegroundColor Green

# 1. Inicia o User Service (Abre noutra janela)
Start-Process cmd -ArgumentList "/k title User Service (8081) && cd user-service && .\mvnw spring-boot:run"

# 2. Inicia o Email Service (Abre noutra janela)
Start-Process cmd -ArgumentList "/k title Email Service (8082) && cd email-service && .\mvnw spring-boot:run"

# 3. Inicia o Frontend (Abre noutra janela)
Start-Process cmd -ArgumentList "/k title Frontend Node.js (3000) && cd frontend-secrest && npm install && node server.js"

Write-Host "Tudo a iniciar! O Frontend estara disponivel em http://localhost:3000" -ForegroundColor Cyan