package th.mfu.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests for book-service.
 * <p>
 * The naming server is not running during a test, so discovery is switched off
 * here. The test still checks that you configured it.
 * <p>
 * This test class is complete. Do not change it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "eureka.client.enabled=false" })
public class BookServiceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Test
    public void itHasANameAndAPort() {
        assertEquals("library-book-service",
                environment.getProperty("spring.application.name"));
        assertEquals("8080", environment.getProperty("server.port"));
    }

    @Test
    public void itJoinsTheNamingServer() {
        // Step 1.
        assertNotNull(AnnotationUtils.findAnnotation(BookServiceApp.class, EnableDiscoveryClient.class),
                "add @EnableDiscoveryClient to BookServiceApp");

        assertEquals("true", environment.getProperty("eureka.client.register-with-eureka"),
                "add the eureka lines to application.properties");
        assertEquals("http://localhost:8761/eureka/",
                environment.getProperty("eureka.client.serviceUrl.defaultZone"));
    }

    @Test
    public void itReturnsOneBook() throws Exception {
        mockMvc.perform(get("/api/books/10002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("1984"))
                .andExpect(jsonPath("$.author").value("George Orwell"))
                .andExpect(jsonPath("$.year").value(1949));
    }

    @Test
    public void itReturnsAllBooks() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    public void anUnknownBookIs404() throws Exception {
        mockMvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void itSaysWhichCopyAnswered() throws Exception {
        // Step 6. Without this the load balancer works, but you cannot see it.
        mockMvc.perform(get("/api/books/10002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servedBy").value(8080));
    }
}
