package th.mfu.transaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * How this service asks library-book-service for a book.
 * <p>
 * SOLUTION for step 3.
 * <p>
 * There is no code in this file, and there is no class implementing it anywhere.
 * Feign reads these annotations at startup and builds the implementation: it
 * makes the URL, sends the GET, and turns the JSON answer into a BookDTO.
 * <p>
 * "library-book-service" is a NAME, not an address. It is the
 * spring.application.name of the other service. The naming server turns it into
 * a real address at the moment of the call - which is why the same name can
 * point at two copies in step 6, with no change here.
 * <p>
 * The path below must match the other service's controller. Nothing checks that
 * for you: if book-service renames its endpoint, this line breaks at run time,
 * not at compile time. That is the price of two separate programs.
 */
@FeignClient(name = "library-book-service")
public interface BookClient {

    @GetMapping("/api/books/{id}")
    BookDTO getBook(@PathVariable("id") Long id);
}
