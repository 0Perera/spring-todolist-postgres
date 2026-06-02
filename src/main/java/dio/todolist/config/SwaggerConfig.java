package dio.todolist.config;



import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ToDo List API - Gerenciador de Tarefas")
                        .version("1.0")
                        .description("""
                                ### Bem-vindo à API de Gerenciamento de Tarefas!

                                Esta API utiliza autenticação via **JWT (Bearer Token)**.


                                **Fluxo Sugerido para Primeiros Passos:**

                                1. **Cadastro**: Acesse a aba **Usuários (`/user`)** e crie uma nova conta usando o método `POST`.

                                2. **Login**: Use a rota **`/auth/login`** com seu e-mail e senha. A resposta retornará um token JWT.

                                3. **Autenticação**: Clique no botão **Authorize** (no topo) e cole o token JWT no campo `bearerAuth` (sem o prefixo "Bearer").

                                4. **Gestão de Tarefas**: Crie suas tarefas em **Tarefas (`/task`)** pelo método `POST`. Após isso, teste buscar todas (`GET`), atualizar status (`PUT`) ou buscar por título (`GET /findByTitle`).

                                """)
                )
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public OpenApiCustomizer customerStepOrder() {
        return openApi -> {
            List<Tag> sortedTags = new ArrayList<>();

            sortedTags.add(new Tag().name("Usuários").description("Gerenciamento dos usuários do sistema"));
            sortedTags.add(new Tag().name("Autenticação").description("Endpoints para login, logout e informações da sessão"));
            sortedTags.add(new Tag().name("Tarefas").description("Gerenciamento das tarefas do usuário"));

            openApi.setTags(sortedTags);
        };
    }

}
