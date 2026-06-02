# ToDo List API - Spring Boot

Um projeto pessoal desenvolvido para colocar em prática e aprofundar os conhecimentos em desenvolvimento Back-end utilizando **Java** e o ecossistema **Spring Boot**.

Esta é uma API RESTful de gerenciamento de tarefas construída com foco em boas práticas de engenharia de software, segurança stateless com JWT, escalabilidade e testes automatizados.

---

## Objetivo do Projeto

O principal objetivo desta aplicação não é apenas ser mais um gerenciador de tarefas, mas sim servir como um laboratório para a aplicação de conceitos avançados e padrões de arquitetura de software exigidos pelo mercado.

**Principais tópicos estudados e aplicados:**
- Criação de APIs RESTful estruturadas
- Autenticação stateless com JWT (JSON Web Tokens)
- Proteção de dados (isolamento de recursos por usuário)
- Tratamento global de exceções
- Testes unitários e de integração
- Mapeamento robusto entre Entidades e DTOs

---

## Tecnologias e Ferramentas

- **Linguagem:** Java 21
- **Framework Principal:** Spring Boot 4.x
- **Persistência de Dados:** Spring Data JPA, Hibernate, PostgreSQL (produção) e H2 (testes)
- **Segurança:** Spring Security + JWT stateless (jjwt), BCrypt
- **Mapeamento de Objetos:** MapStruct (Entity ↔ DTO)
- **Redução de Boilerplate:** Lombok
- **Validação:** Jakarta Validation
- **Testes:** JUnit 5, Mockito e Spring MockMvc
- **Documentação:** Springdoc OpenAPI 3 (Swagger UI)

---

## Documentação da API (Swagger)

A API possui documentação interativa com Swagger/OpenAPI. Você pode visualizar todos os endpoints, seus schemas e testar requisições diretamente pelo navegador.

> A documentação não está hospedada na nuvem. É necessário iniciar a aplicação localmente.

**1.** Rode o projeto:
```bash
./mvnw spring-boot:run
```

**2.** Acesse o Swagger UI:
**[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

**Fluxo de autenticação no Swagger:**
1. Cadastre um usuário via `POST /user`
2. Faça login via `POST /auth/login` — a resposta retorna um `token` JWT
3. Clique em **Authorize** no topo do Swagger UI, cole o token no campo `bearerAuth` e confirme
4. Todas as rotas protegidas estarão liberadas para teste

---

## Arquitetura e Boas Práticas

- **Arquitetura em Camadas:** Divisão clara entre `Controller` (camada web), `Service` (regras de negócio) e `Repository` (acesso a dados).
- **Padrão DTO:** Implementado com `Records` do Java para imutabilidade e tráfego seguro de dados, sem expor entidades do banco na web.
- **Autenticação JWT Stateless:** O `JwtAuthenticationFilter` intercepta cada requisição, valida o token Bearer e popula o `SecurityContextHolder`. Nenhuma sessão é mantida no servidor (`SessionCreationPolicy.STATELESS`).
- **Ownership / Isolamento por usuário:** O `TaskService.validateOwnership()` compara o dono da tarefa com o usuário autenticado via `@AuthenticationPrincipal`. Qualquer divergência lança `AccessDeniedException` (403).
- **Tratamento Global de Exceções:** O `@RestControllerAdvice` (`GlobalExceptionHandler`) mapeia exceções de domínio para respostas HTTP corretas — `NotFoundException` → 404, `AccessDeniedException` → 403, `DuplicateEmailException` → 409, `BadCredentialsException` → 401.
- **Conformidade REST:** Verbos HTTP corretos, query params em `GET`, e status codes assertivos (`201 Created`, `204 No Content`, etc.).
- **Cobertura de Testes:** `@WebMvcTest` para controllers, `@DataJpaTest` para repositórios, `@ExtendWith(MockitoExtension.class)` para serviços e filtros, com H2 em memória para os testes de integração.

---

## Endpoints da API

### Autenticação — `/auth`

| Método | Rota | Autenticação | Descrição |
|--------|------|:---:|-----------|
| `POST` | `/auth/login` | Não | Autentica e retorna um token JWT |
| `POST` | `/auth/logout` | Não | Instrução de logout (stateless — descarte o token no cliente) |
| `GET` | `/auth/me` | Opcional | Retorna o e-mail do usuário autenticado pelo token atual |

**Body de login (`POST /auth/login`):**
```json
{ "email": "usuario@email.com", "password": "suaSenha" }
```
**Resposta:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

---

### Usuários — `/user`

| Método | Rota | Autenticação | Descrição |
|--------|------|:---:|-----------|
| `POST` | `/user` | Não | Cadastra um novo usuário |
| `GET` | `/user/{id}` | Sim | Busca um usuário pelo ID |
| `PUT` | `/user/{id}` | Sim | Atualiza dados do usuário (nome, e-mail ou senha) |
| `DELETE` | `/user/{id}` | Sim | Remove o usuário permanentemente |

---

### Tarefas — `/task`

Todos os endpoints de tarefas exigem autenticação. Cada usuário acessa apenas suas próprias tarefas.

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/task` | Cria uma tarefa (status inicial: `PENDENTE`) |
| `GET` | `/task` | Lista todas as tarefas do usuário (paginado) |
| `GET` | `/task/findByTitle?title=...` | Busca uma tarefa pelo título exato |
| `GET` | `/task/status/{status}` | Lista tarefas por status (`PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`) |
| `PUT` | `/task/{id}` | Atualiza título, descrição ou status (atualização parcial) |
| `DELETE` | `/task/{id}` | Remove uma tarefa permanentemente |

---

## Como rodar o projeto localmente

### Pré-requisitos
- Java 21+
- Maven (opcional — o projeto usa o wrapper `mvnw`)
- PostgreSQL

### Passos

**1.** Clone o repositório:
```bash
git clone https://github.com/0Perera/spring-todolist-postgres
```

**2.** Configure as variáveis de ambiente (ou exporte antes de rodar):

| Variável | Descrição |
|----------|-----------|
| `DB_URL` | URL JDBC do PostgreSQL (ex: `jdbc:postgresql://localhost:5432/todolist`) |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_KEY` | Chave secreta HMAC em Base64 para assinar os tokens JWT |

**3.** Inicie a aplicação:
```bash
./mvnw spring-boot:run
```

**4.** A API estará disponível em `http://localhost:8080`.

---

## Como rodar os testes

Os testes usam H2 em memória — `DB_URL`, `DB_USER` e `DB_PASSWORD` não são necessárias. A variável `JWT_KEY` ainda é exigida, pois os testes usam a mesma configuração de segurança da produção.

```bash
# Todos os testes
./mvnw test

# Uma classe específica
./mvnw test -Dtest=TaskServiceTest

# Um método específico
./mvnw test -Dtest=TaskServiceTest#createCase1
```

---

Feito para aprimoramento técnico de Back-end com Java.
