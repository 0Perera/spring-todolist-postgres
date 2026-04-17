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

                                Esta API permite que usuários gerenciem suas listas de tarefas diárias de forma segura e prática.


                                **📌 Fluxo Sugerido para Primeiros Passos:**
                                
                                1. **Cadastro**: Acesse a aba **Usuários (`/user`)** e crie uma nova conta usando o método `POST`.
                                
                                2. **Autenticação**: Não se esqueça de clicar no botão **Authorize** (no topo) para configurar o esquema `basicAuth` com o e-mail e senha que você acabou de criar.
                                
                                3. **Acesso**: Uma vez autenticado, você já pode acessar a rota **Autenticação (`/auth/login`)** ou tentar consumir os endpoints protegidos.
                                
                                4. **Gestão de Tarefas**: Crie suas tarefas em **Tarefas (`/task`)** pelo método `POST`. Após isso, teste buscar todas (`GET`), atualizar status (`PUT`) ou buscar por título (`GET /findByTitle`).
                               
                                """)
                )
        .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
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
