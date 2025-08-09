package rw.health.ubuzima.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ubuzima Family Planning API")
                        .description("REST API for Ubuzima Family Planning Mobile Application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ubuzima Development Team")
                                .email("dev@ubuzima.rw")
                                .url("https://ubuzima.rw"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api/v1")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.ubuzima.rw/v1")
                                .description("Production Server")
                ));
    }
}
