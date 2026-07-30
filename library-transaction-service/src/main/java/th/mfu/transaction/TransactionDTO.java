package th.mfu.transaction;

/**
 * What a transaction looks like on the wire.
 * <p>
 * The client sends: type, bookId, borrowerName.
 * <p>
 * The answer adds bookTitle and servedBy. Neither of those is in our database -
 * both come back from library-book-service. That is the whole reason this
 * service has to talk to another one.
 */
public class TransactionDTO {

    private Long id;
    private String type;
    private Long bookId;
    private String borrowerName;

    /** Filled in from the answer of library-book-service. */
    private String bookTitle;

    /**
     * Which copy of library-book-service answered. See step 6.
     * <p>
     * Integer, not int, on purpose. GET /api/transactions never calls
     * book-service, so nothing served those rows. An int would print 0 there,
     * which reads like a port number and is not one. Integer prints null, which
     * is the truth.
     */
    private Integer servedBy;

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

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Integer getServedBy() {
        return servedBy;
    }

    public void setServedBy(Integer servedBy) {
        this.servedBy = servedBy;
    }
}
