package br.edu.ifsp.conectaavida.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CONFIGURAÇÃO DE CORS (Cross-Origin Resource Sharing)
 *
 * Explicação para a Equipe:
 * Navegadores web (como o Chrome) têm uma trava de segurança que impede que um site
 * rodando na porta 5173 (React) faça requisições para uma API na porta 8080 (Spring).
 * Esta classe destranca essa porta, permitindo que o Painel Admin converse com a nossa API.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Lê a lista de URLs permitidas lá do nosso arquivo application-admin.properties
    @Value("${app.security.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Libera todas as rotas da Admin API (ex: /api/usuarios, /api/alertas)
                .allowedOrigins(allowedOrigins) // Diz quais sites podem acessar
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Libera os verbos HTTP
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}