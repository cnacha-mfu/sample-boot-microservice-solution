package th.mfu.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import feign.FeignException;
import feign.Request;
import feign.Response;

/**
 * Tests for transaction-service.
 * <p>
 * book-service is not running during a test, so the Feign client is replaced by
 * a fake one. The fake answers "book 10002 is called 1984, and port 8090
 * answered".
 * <p>
 * This test class is complete. Do not change it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "eureka.client.enabled=false" })
public class TransactionServiceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @MockBean
    private BookClient bookClient;

    private static final String BORROW_JSON =
            "{\"type\":\"BORROW\",\"bookId\":10002,\"borrowerName\":\"Alice Johnson\"}";

    private BookDTO aBook() {
        BookDTO book = new BookDTO();
        book.setId(10002L);
        book.setTitle("1984");
        book.setAuthor("George Orwell");
        book.setYear(1949);
        book.setServedBy(8090);
        return book;
    }

    /** Builds the exception Feign throws when the other service answers 404. */
    private FeignException notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/books/999999",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(Collections.emptyMap())
                .build();
        return FeignException.errorStatus("BookClient#getBook(Long)", response);
    }

    @Test
    public void itHasANameAndAPort() {
        assertEquals("library-transaction-service",
                environment.getProperty("spring.application.name"));
        assertEquals("8100", environment.getProperty("server.port"));
    }

    @Test
    public void itJoinsTheNamingServer() {
        // Step 2.
        assertNotNull(AnnotationUtils.findAnnotation(
                TransactionServiceApp.class, EnableDiscoveryClient.class),
                "add @EnableDiscoveryClient to TransactionServiceApp");

        assertEquals("true", environment.getProperty("eureka.client.register-with-eureka"));
        assertEquals("true", environment.getProperty("eureka.client.fetch-registry"),
                "this service must READ the list to find library-book-service");
    }

    @Test
    public void feignClientsAreSwitchedOn() {
        // Step 3.
        assertNotNull(AnnotationUtils.findAnnotation(
                TransactionServiceApp.class, EnableFeignClients.class),
                "add @EnableFeignClients to TransactionServiceApp");
    }

    @Test
    public void theBookClientAsksForAServiceByName() {
        // Step 3.
        FeignClient annotation = AnnotationUtils.findAnnotation(BookClient.class, FeignClient.class);

        assertNotNull(annotation, "add @FeignClient to BookClient");
        assertEquals("library-book-service", annotation.name());
        assertEquals("", annotation.url(),
                "use the service name only. A fixed URL cannot be load balanced");
    }

    @Test
    public void itRecordsABorrowAndAddsWhatTheOtherServiceKnows() throws Exception {
        // Step 4.
        when(bookClient.getBook(any())).thenReturn(aBook());

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON).content(BORROW_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("BORROW"))
                .andExpect(jsonPath("$.bookId").value(10002))
                .andExpect(jsonPath("$.borrowerName").value("Alice Johnson"))
                // these two are not in our database: they came back from
                // library-book-service
                .andExpect(jsonPath("$.bookTitle").value("1984"))
                .andExpect(jsonPath("$.servedBy").value(8090));
    }

    @Test
    public void anUnknownBookIs400() throws Exception {
        // Step 5. The other service answers 404, and Feign turns that into an
        // exception instead of an empty Optional.
        when(bookClient.getBook(any())).thenThrow(notFound());

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON).content(BORROW_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void bookServiceBeingDownIs503() throws Exception {
        // Step 5. A different failure, so a different answer: we cannot say the
        // request was wrong, only that we cannot serve it right now.
        when(bookClient.getBook(any())).thenThrow(new RuntimeException("connection refused"));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON).content(BORROW_JSON))
                .andExpect(status().isServiceUnavailable());
    }
}
