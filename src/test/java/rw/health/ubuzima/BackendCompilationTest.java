package rw.health.ubuzima;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class BackendCompilationTest {

    @Test
    public void contextLoads() {
        // This test will pass if the Spring context loads successfully
        // which means all beans are properly configured and there are no compilation errors
    }
}
