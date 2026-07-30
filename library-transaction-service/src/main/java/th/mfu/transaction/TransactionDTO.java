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

    /** Which copy of library-book-service answered. See step 6. */
    private int servedBy;

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

    public int getServedBy() {
        return servedBy;
    }

    public void setServedBy(int servedBy) {
        this.servedBy = servedBy;
    }
}
