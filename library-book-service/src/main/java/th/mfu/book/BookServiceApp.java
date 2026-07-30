package th.mfu.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * book-service. Port 8090.
 * <p>
 * SOLUTION for step 1.
 * <p>
 * &#64;EnableDiscoveryClient is what makes this service say "I am here, and my
 * name is library-book-service" to the naming server. Without it the service
 * still works - you can call it on port 8090 all day - but nobody can find it by
 * name, and step 6 becomes impossible.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BookServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApp.class, args);
    }
}
