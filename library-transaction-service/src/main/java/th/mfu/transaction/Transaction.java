package th.mfu.transaction;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * One borrow or return.
 * <p>
 * LOOK AT THE bookId FIELD. In the 3-tier sample this was:
 *
 * <pre>
 *   &#64;ManyToOne
 *   private Book book;
 * </pre>
 *
 * It cannot be that any more. Book lives in another service now, in another
 * database, and JPA cannot join across two databases. So the relationship
 * becomes a plain number, and the join becomes an HTTP call.
 * <p>
 * That one change is what "microservice" costs you, and it is worth saying out
 * loud: the database can no longer check that the book exists. Our code has to.
 */
@Entity
@Table(name = "transaction")
public class Transaction {

    public static final String TYPE_BORROW = "BORROW";
    public static final String TYPE_RETURN = "RETURN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private LocalDate transactionDate;

    /** The id of a book that lives in library-book-service. */
    private Long bookId;

    private String borrowerName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }
}
