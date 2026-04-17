# 📝 ToDo List API - Spring Boot

Um projeto pessoal desenvolvido para colocar em prática e aprofundar os conhecimentos em desenvolvimento Back-end utilizando **Java** e o ecossistema **Spring Boot**. 

Este projeto é uma API RESTful de gerenciamento de tarefas (To-Do List), construída com foco em boas práticas de engenharia de software, segurança, escalabilidade e testes automatizados.

---

## 🎯 Objetivo do Projeto
O principal objetivo desta aplicação não é apenas ser mais um gerenciador de tarefas, mas sim servir como um "laboratório" para a aplicação de conceitos avançados e padrões de arquitetura de software exigidos pelo mercado.

**Principais tópicos estudados e aplicados:**
- Criação de APIs RESTful estruturadas.
- Autenticação e Segurança.
- Proteção de dados (Isolamento de recursos por usuário).
- Tratamento global de exceções.
- Testes unitários e de integração.
- Mapeamento robusto entre Entidades e DTOs.

---

## 🚀 Tecnologias e Ferramentas

O projeto foi desenvolvido utilizando uma stack moderna e amplamente utilizada no mercado:

- **Linguagem:** Java 21
- **Framework Principal:** Spring Boot 3.x
- **Persistência de Dados:** Spring Data JPA, Hibernate, PostgreSQL (Produção) e H2 Database (Testes)
- **Segurança:** Spring Security (Criptografia com BCrypt)
- **Mapeamento de Objetos:** MapStruct (Entity ↔ DTO)
- **Redução de Boilerplate:** Lombok
- **Validação:** Jakarta Validation
- **Testes:** JUnit 5, Mockito e Spring MockMvc
- **Documentação:** Springdoc OpenAPI 3 (Swagger UI)

---

## 📖 Documentação da API (Swagger)

A API possui uma documentação viva e interativa criada com o Swagger/OpenAPI. Através dela, você pode visualizar todos os endpoints disponíveis, seus respectivos schemas (DTOs) e até mesmo testar as requisições diretamente pelo navegador.

⚠️ **Importante:** A documentação não está hospedada na nuvem. Para visualizá-la, você precisa primeiro **iniciar a aplicação localmente**. 

**1.** Rode o projeto na sua máquina:
```bash
./mvnw spring-boot:run
```

**2.** Uma vez que o servidor inicie com sucesso, acesse a interface visual pelo seu navegador através do link:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

**Dica de Uso:** 
- Cadastre um usuário pela aba de testes no Swagger ou via Postman.
- Clique no botão **Authorize** (no topo do Swagger UI) e insira as credenciais do usuário cadastrado na opção `basicAuth` para liberar e testar todas as rotas protegidas.

---

## ⚙️ Arquitetura e Boas Práticas Avaliadas

Para garantir um código limpo e de fácil manutenção, o projeto segue os seguintes padrões:

- **Arquitetura em Camadas (Layered Architecture):** Divisão clara de responsabilidades entre `Controller` (camada web), `Service` (regras de negócio) e `Repository` (acesso a dados).
- **Padrão DTO (Data Transfer Object):** Implementado utilizando os `Records` do Java para garantir imutabilidade e tráfego seguro de informações, sem expor entidades do banco de dados na web.
- **Multitenancy Lógico / Ownership:** A camada de serviço garante via `SecurityContextHolder` que um usuário só pode visualizar, editar e deletar as **suas próprias tarefas**. O isolamento de dados é reforçado por métodos como `validateOwnership()`.
- **Tratamento de Exceções Global:** Implementado um `@RestControllerAdvice` (`GlobalExceptionHandler`) para capturar exceções da aplicação (como `NotFoundException` ou `AccessDeniedException`) e transformá-las em respostas HTTP modeladas corretamente (ex: 404 Not Found, 403 Forbidden).
- **Conformidade REST:** Uso adequado dos verbos HTTP, envio correto de dados via *Query Parameters* em requisições `GET` (como filtros de busca), e retorno assertivo de Status Codes (ex: `204 No Content` para sucesso em deleções e `201 Created` para criações).
- **Cobertura de Testes Automatizados:** Testes integrados utilizando `@SpringBootTest` e `@MockitoBean` nas camadas de `Service` (testando as regras de negócio com o contexto da aplicação), testes de persistência isolada na camada `Repository` com `@DataJpaTest`, e testes de integração da API na camada `Controller` validando o comportamento de ponta-a-ponta utilizando `@WebMvcTest` e MockMvc.

---

## 📋 Funcionalidades da API

A API permite:
1. **Autenticação:** Login de usuário e proteção de rotas.
2. **Gerenciamento de Tarefas:** Adicionar novas tarefas passando Nome e Descrição.
3. **Controle de Status:** Atualizar parcialmente uma tarefa utilizando status de andamento (ex: `PENDENTE`, `CONCLUIDA`).
4. **Filtros e Consultas:** Buscar todas as tarefas do usuário autenticado, pesquisar por título ou listar tarefas passando um status específico.
5. **Manutenção:** Atualizar descrições, títulos e remover rotinas concluídas.

---

## 🛠️ Como rodar o projeto localmente

### Pré-requisitos
- Java 21+ instalado.
- Maven (Opcional, o projeto usa o Maven Wrapper `mvnw`).
- Servidor PostgreSQL (Caso prefira o H2, basta ajustar as `application.properties`).

### Passos

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/SeuUsuario/ToDoList.git
   cd ToDoList
   ```

2. **Configure o Banco de Dados:**
   Abra o diretório `src/main/resources` e verifique as configurações no arquivo `application.properties` para alinhar com o acesso do seu SGBD local.

3. **Inicie a aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. A API estará disponível no endereço `http://localhost:8080`.

---

## 🧪 Como rodar os testes

Os testes automatizados cobrem desde a persistência até as requisições da camada web (via MockMvc) subindo o contexto da aplicação com o banco em memória (H2). Para executá-los, rode:
```bash
./mvnw test
```

---

## 💡 Próximos Passos (Roadmap)
Como todo projeto de estudo, sempre há espaço para evoluir. Algumas das melhorias planejadas:
- Implementação de JWT (JSON Web Tokens) em vez de Basic Auth/Session.
- Dockerização da API e do banco de dados (Criação de `docker-compose.yml`).

---
Feito com dedicação para aprimoramento técnico de Back-end com Java. ☕
