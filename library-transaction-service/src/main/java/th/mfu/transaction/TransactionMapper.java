package th.mfu.transaction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Assembler for Transaction. Same annotations as the 3-tier sample.
 * <p>
 * Put the 3-tier TransactionMapper on screen beside this one. There, four
 * &#64;Mapping lines flattened two relationships:
 *
 * <pre>
 *   &#64;Mapping(source = "book.title",  target = "bookTitle")
 *   &#64;Mapping(source = "member.name", target = "memberName")
 * </pre>
 *
 * Here bookTitle is <b>ignored</b> instead. Not because we stopped wanting it,
 * but because there is no book to read it from: the entity holds a bookId and
 * nothing else. What a mapper did for free in one program, an HTTP call has to
 * do now - and the controller does it, one row at a time.
 * <p>
 * That single changed line is the cost of splitting the application, written
 * down in the smallest place it shows up.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Entity out to the wire. bookTitle and servedBy are not ours to fill - they
     * come back from library-book-service, so the controller sets them after
     * this returns.
     */
    @Mapping(target = "bookTitle", ignore = true)
    @Mapping(target = "servedBy", ignore = true)
    TransactionDTO toDto(Transaction entity);

    /**
     * Wire in to a new row. The id is the database's to choose, and the date is
     * the server's - never the caller's, or a client could backdate a borrow.
     * The controller sets the date straight after this returns.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    Transaction toEntity(TransactionDTO dto);
}
