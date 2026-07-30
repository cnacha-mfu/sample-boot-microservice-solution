package th.mfu.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The REST service for books.
 * <p>
 * SOLUTION for step 6.
 * <p>
 * Nothing here is new - it is the same kind of controller as in the 3-tier
 * sample. What is new is its role: this is the service that ANOTHER service
 * calls.
 */
@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    /**
     * Tells us the port this copy of the service is running on.
     */
    @Autowired
    private Environment environment;

    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> listBooks() {
        List<BookDTO> books = new ArrayList<>();
        for (Book book : bookRepository.findAll()) {
            books.add(toDto(book));
        }
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    /**
     * The endpoint BookClient calls from the other service. When it answers 404,
     * Feign turns that into a thrown exception over there.
     */
    @GetMapping("/books/{id}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Long id) {
        Optional<Book> book = bookRepository.findById(id);
        if (!book.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(toDto(book.get()), HttpStatus.OK);
    }

    /**
     * Entity in, DTO out. Written by hand: you already know how to let MapStruct
     * do this, and today is not about that.
     */
    private BookDTO toDto(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setYear(book.getYear());

        /*
         * Step 6. Stamp the answer with the port of THIS copy.
         *
         * The load balancer works without this line - but you cannot SEE it
         * working, and a lesson you cannot see is a lesson nobody believes. Run
         * two copies of this service and every answer will name the one that did
         * the work.
         *
         * Real code would not put an internal port on the wire.
         */
        dto.setServedBy(Integer.parseInt(environment.getProperty("server.port")));

        return dto;
    }
}
