package th.mfu.book;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Assembler for Book. Same idea, same annotations, as the 3-tier sample -
 * you write the interface, MapStruct writes the class at compile time.
 * <p>
 * After a build, go and read what it produced:
 *
 * <pre>
 *   library-book-service/target/generated-sources/annotations/
 *       th/mfu/book/BookMapperImpl.java
 * </pre>
 *
 * There is one difference worth noticing. In the 3-tier sample the &#64;Mapping
 * lines flattened a relationship - category.id became categoryId. There is no
 * relationship to flatten here, because Book has none: the only thing it could
 * point at lives in another service now. The mapper got simpler because the
 * design got more spread out.
 */
@Mapper(componentModel = "spring")
public interface BookMapper {

    /**
     * Two sources, one DTO. id, title, author and year come from the book; the
     * port is handed in, because it is not a property of the book at all - it
     * says WHICH COPY of this service answered, and only the controller knows
     * that.
     * <p>
     * Taking it as a parameter keeps it in one place. Setting it after the call
     * instead would mean writing the same line in every endpoint, and fixing
     * only one of them is a bug nobody sees until step 6.
     */
    @Mapping(target = "servedBy", source = "port")
    BookDTO toDto(Book book, int port);
}
