# finnza-backend

Backend do sistema financeiro Finnza desenvolvido com Spring Boot.

## Tecnologias

- Spring Boot 3.3.5
- Java 17
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL / H2

## Funcionalidades

- Autenticação e autorização com JWT
- Gerenciamento de usuários (CRUD completo)
- Sistema de permissões por módulo
- Soft delete
- Filtros avançados de busca
- Segurança com BCrypt para senhas

## Estrutura

```
com.finnza/
├── domain/entity      # Entidades JPA
├── dto               # DTOs de requisição e resposta
├── repository        # Repositories e Specifications
├── service           # Lógica de negócio
├── controller        # Endpoints REST
├── security          # Configurações de segurança
└── exception         # Tratamento de exceções
```

## Configuração

Configure as variáveis de ambiente ou edite `application.properties`:

- `jwt.secret`: Chave secreta para JWT
- `spring.datasource.*`: Configurações do banco de dados

## Executar

```bash
mvn spring-boot:run
```

# finnza-backend
