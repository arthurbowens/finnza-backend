-- Script de inicialização do banco de dados PostgreSQL
-- Execute este script no PostgreSQL para criar o banco de dados

-- Criar banco de dados (execute como superusuário)
CREATE DATABASE finnza_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'pt_BR.UTF-8'
    LC_CTYPE = 'pt_BR.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Conectar ao banco finnza_db antes de executar os próximos comandos
-- \c finnza_db

-- As tabelas serão criadas automaticamente pelo Hibernate
-- com a configuração spring.jpa.hibernate.ddl-auto=update

