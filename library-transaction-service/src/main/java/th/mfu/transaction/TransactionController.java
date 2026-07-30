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
     * The Assembler, exactly as in the 3-tier sample. It fills in everything this
     * service already knows; bookClient above fetches the one field it does not.
     */
    @Autowired
    private TransactionMapper transactionMapper;

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

        Transaction transaction = transactionMapper.toEntity(dto);
        // The mapper ignores the date on purpose. When a borrow happened is ours
        // to decide, not the caller's.
        transaction.setTransactionDate(LocalDate.now());
        Transaction saved = transactionRepository.save(transaction);

        TransactionDTO result = transactionMapper.toDto(saved);
        // These two fields are not in our database. They came back from the other
        // service, which is the whole reason we called it.
        result.setBookTitle(book.getTitle());
        result.setServedBy(book.getServedBy());

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * GET /api/transactions - list what we have recorded.
     * <p>
     * Our database holds a bookId and nothing else about the book, so the title
     * has to be asked for. One row, one call.
     * <p>
     * SAY THIS OUT LOUD: ten transactions means ten HTTP requests. In the 3-tier
     * sample the database did this with a join, once. This is the same N+1
     * problem you already know, except every "+1" is now a network request that
     * can be slow or fail.
     * <p>
     * It is left this way because it is honest and it is easy to read. The fixes
     * - ask book-service for many books at once, or keep a copy of the title
     * here - are what the next lesson is for.
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> list() {
        List<TransactionDTO> result = new ArrayList<>();
        for (Transaction transaction : transactionRepository.findAll()) {

            TransactionDTO dto = transactionMapper.toDto(transaction);
            try {
                BookDTO book = bookClient.getBook(transaction.getBookId());
                dto.setBookTitle(book.getTitle());
                dto.setServedBy(book.getServedBy());

            } catch (Exception e) {
                /*
                 * One unreachable book must not empty the whole list.
                 *
                 * The POST answers 503 when book-service is down, because a
                 * borrow that was not checked is worthless. A list is not: the
                 * transactions are still true, we just cannot name the books. So
                 * we return what we have and leave bookTitle null.
                 *
                 * Same failure, different answer, because the two requests are
                 * worth different things. That judgement is the interesting part.
                 */
                LOGGER.warn("no title for book {}: {}", transaction.getBookId(), e.getMessage());
            }
            result.add(dto);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
