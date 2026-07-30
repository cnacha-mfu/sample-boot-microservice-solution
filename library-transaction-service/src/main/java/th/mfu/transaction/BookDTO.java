package th.mfu.transaction;

/**
 * A book, as this service receives it from library-book-service.
 * <p>
 * Yes - this is the second copy of BookDTO. The other one is in
 * library-book-service. Keeping two copies looks wasteful, and it is the point:
 * the two services are separate programs. They agree on a shape, not on a class.
 * <p>
 * Share one jar instead and you have tied the two services together: every
 * change to the shape forces both of them to be rebuilt and redeployed at the
 * same time, which is exactly what you were trying to get away from.
 * <p>
 * Note also that this copy does not have to be complete. If book-service adds a
 * field tomorrow, this class simply ignores it.
 */
public class BookDTO {

    private Long id;
    private String title;
    private String author;
    private int year;
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
