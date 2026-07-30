package th.mfu.transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;

/**
 * Records a borrow or a return.
 * <p>
 * SOLUTION for step 4 and step 5.
 * <p>
 * Compare the one important line with the 3-tier sample:
 *
 * <pre>
 *   Optional&lt;Book&gt; book = bookRepository.findById(id);   // 3-tier: one database
 *   BookDTO       book = bookClient.getBook(id);         // now: another program
 * </pre>
 *
 * One line for one line - and everything else in this class exists because the
 * second one goes over a network.
 */
@RestController
@RequestMapping("/api")
public class TransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Step 3 built this for us. There is no class to instantiate: Feign made one
     * at startup, so it can be injected like any other bean.
     */
    @Autowired
    private BookClient bookClient;

    /**
     * POST /api/transactions - record a borrow or a return.
     */
    @PostMapping("/transactions")
    public ResponseEntity<TransactionDTO> record(@RequestBody TransactionDTO dto) {

        BookDTO book;
        try {
            // One line, and an HTTP request to another program. Watch the console
            // of book-service: the call arrives there.
            book = bookClient.getBook(dto.getBookId());

        } catch (FeignException.NotFound e) {
            /*
             * Step 5a. book-service answered 404.
             *
             * In the 3-tier app this was an empty Optional. Feign does not return
             * one - it THROWS. The caller asked for a book that does not exist,
             * so this is their mistake: 400.
             */
            LOGGER.warn("book {} does not exist", dto.getBookId());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            /*
             * Step 5b. book-service did not answer at all: it is stopped, or it
             * never registered, or it took longer than the two-second timeout.
             *
             * This is NOT the caller's fault, so 400 would be a lie. 503 says the
             * true thing: you asked correctly, I cannot do it right now.
             *
             * This whole catch block is the price of splitting the application.
             * The 3-tier version could not fail this way.
             */
            LOGGER.error("book-service did not answer: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }

        Transaction saved = transactionRepository.save(toEntity(dto));

        TransactionDTO result = toDto(saved);
        // These two fields are not in our database. They came back from the other
        // service, which is the whole reason we called it.
        result.setBookTitle(book.getTitle());
        result.setServedBy(book.getServedBy());

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * GET /api/transactions - list what we have recorded.
     * <p>
     * This one needs no help from anybody: every field it returns is ours. So
     * there is no bookTitle here - compare it with the answer to the POST.
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> list() {
        List<TransactionDTO> result = new ArrayList<>();
        for (Transaction transaction : transactionRepository.findAll()) {
            result.add(toDto(transaction));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private TransactionDTO toDto(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setBookId(transaction.getBookId());
        dto.setBorrowerName(transaction.getBorrowerName());
        return dto;
    }

    private Transaction toEntity(TransactionDTO dto) {
        Transaction transaction = new Transaction();
        transaction.setType(dto.getType());
        transaction.setBookId(dto.getBookId());
        transaction.setBorrowerName(dto.getBorrowerName());
        transaction.setTransactionDate(LocalDate.now());
        return transaction;
    }
}
