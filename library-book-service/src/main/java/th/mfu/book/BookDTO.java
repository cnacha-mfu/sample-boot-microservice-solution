package th.mfu.book;

/**
 * What a book looks like ON THE WIRE.
 * <p>
 * There is a second copy of this class in library-transaction-service. That is
 * deliberate, and it is worth a minute of your attention: the two services are
 * separate programs that only agree on a shape. Sharing one jar between them
 * would tie them together again, and then you would have two services that must
 * always be rebuilt and redeployed as a pair - which is the thing microservices
 * are trying to avoid.
 */
public class BookDTO {

    private Long id;
    private String title;
    private String author;
    private int year;

    /**
     * Which copy of book-service answered. It is here only so that you can SEE
     * the load balancer working in step 6. Real code would not put this on the
     * wire.
     */
    private int servedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getServedBy() {
        return servedBy;
    }

    public void setServedBy(int servedBy) {
        this.servedBy = servedBy;
    }
}
