
# 🛡️ Ecossistema Secrest - API Rest com Spring Security (Etapa 4)

Este projeto implementa uma arquitetura de microsserviços segura, utilizando autenticação baseada em JWT, mensageria assíncrona com RabbitMQ e um frontend em Node.js. 

## 🚀 Como Iniciar o Projeto (Script Automatizado)

Para facilitar a execução de toda a arquitetura, foi criado um script em PowerShell.
Na raiz do projeto, execute o ficheiro:
```powershell
.\iniciar.ps1
O script irá abrir três janelas de terminal independentes, iniciando o user-service (8081), o email-service (8082) e o frontend-secrest (3000) automaticamente.

🏗️ Arquitetura do Sistema
User Service (Java/Spring Boot): Responsável pela gestão de utilizadores, autenticação (Spring Security), geração de tokens JWT e envio de eventos para a fila. Banco de Dados: ms_user (MySQL).

Email Service (Java/Spring Boot): Consumidor da fila RabbitMQ que processa o envio de e-mails com os códigos de verificação. Banco de Dados: ms_email (MySQL).

Frontend (Node.js/Express): Interface web interativa que consome as APIs, gere o armazenamento do JWT no sessionStorage e roteia os pedidos protegidos.

## 🗄️ Estrutura do Banco de Dados (MySQL)

O sistema utiliza a estratégia `ddl-auto=update` do Hibernate para gerar as tabelas automaticamente. No entanto, abaixo estão os scripts SQL representativos da modelagem de dados utilizada neste ecossistema:

1. Banco de Dados: ms_user (Serviço de Utilizadores)
SQL
-- Criação do banco de dados
CREATE DATABASE IF NOT EXISTS ms_user;
USE ms_user;

-- Tabela de Usuários
CREATE TABLE tb_users (
    id BINARY(16) NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    password VARCHAR(255) NOT NULL
);

-- Tabela de Cargos (Roles)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela de Relacionamento (Muitos para Muitos)
CREATE TABLE tb_users_roles (
    user_id BINARY(16) NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- INSERÇÃO DOS DADOS INICIAIS (Crucial para o sistema funcionar!)
INSERT INTO roles (name) VALUES ('ROLE_CUSTOMER');
INSERT INTO roles (name) VALUES ('ROLE_ADMINISTRATOR');
2. Banco de Dados: ms_email (Serviço de E-mails)
SQL
-- Criação do banco de dados
CREATE DATABASE IF NOT EXISTS ms_email;
USE ms_email;

-- Tabela de Histórico de E-mails
CREATE TABLE tb_emails (
    email_id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16),
    email_from VARCHAR(255) NOT NULL,
    email_to VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    send_date_email DATETIME NOT NULL,
    status_email TINYINT NOT NULL -- 0 para SENT, 1 para ERROR (Baseado no seu Enum)
);