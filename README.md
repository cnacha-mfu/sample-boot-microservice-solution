# sample-boot-microservice — **SOLUTION**

The finished version of
[`sample-boot-microservice`](https://github.com/cnacha-mfu/sample-boot-microservice).

Every step in that repo's [TODO.md](https://github.com/cnacha-mfu/sample-boot-microservice/blob/master/TODO.md)
links to the file here that answers it.

---

## What is different from the starting point

Seven files. Everything else — the entities, the repositories, the DTOs, the
tests, the Postman collection — is identical, so the two repos can be compared
directly:

```bash
diff -r --exclude=.git --exclude=target --exclude=README.md --exclude=TODO.md \
  sample-boot-microservice sample-boot-microservice-solution
```

| File | Answers |
| --- | --- |
| `library-book-service/.../BookServiceApp.java` | step 1 |
| `library-book-service/src/main/resources/application.properties` | step 1 |
| `library-book-service/.../BookController.java` | step 6 |
| `library-transaction-service/.../TransactionServiceApp.java` | steps 2, 3 |
| `library-transaction-service/src/main/resources/application.properties` | steps 2, 5 |
| `library-transaction-service/.../BookClient.java` | step 3 |
| `library-transaction-service/.../TransactionController.java` | steps 4, 5 |

Each one has comments explaining **why** the code is what it is — they are meant
to be read out during the session.

## Running it

```bash
mvn install -DskipTests
mvn test                 # 15 tests, all green
```

```
Tests run: 2, Failures: 0, Errors: 0 - in th.mfu.namingserver.NamingServerTests
Tests run: 6, Failures: 0, Errors: 0 - in th.mfu.book.BookServiceTests
Tests run: 7, Failures: 0, Errors: 0 - in th.mfu.transaction.TransactionServiceTests
BUILD SUCCESS
```

Tested with Maven 3.9 on JDK 11 and JDK 18. No database to install: each service
uses its own in-memory H2.

To run the whole thing, in this order:

```bash
mvn -pl naming-server                spring-boot:run    # 8761
mvn -pl library-book-service         spring-boot:run    # 8080
mvn -pl library-transaction-service  spring-boot:run    # 8100
mvn -pl library-book-service spring-boot:run -Dspring-boot.run.arguments=--server.port=8081   # step 6
```

Then import `postman/library-microservice.postman_collection.json` and work down
the list. Post the borrow request repeatedly and watch `servedBy` alternate.

## The answer, in short

```java
// BookServiceApp + TransactionServiceApp
@EnableDiscoveryClient        // put me on the naming server's list
@EnableFeignClients           // transaction-service only: build the @FeignClient interfaces

// BookClient - a name, never a URL
@FeignClient(name = "library-book-service")
public interface BookClient {
    @GetMapping("/api/books/{id}")
    BookDTO getBook(@PathVariable("id") Long id);
}

// TransactionController - the line the whole session is about
BookDTO book = bookClient.getBook(dto.getBookId());
// ...and the two catch blocks that exist because it goes over a network
catch (FeignException.NotFound e) { return 400; }
catch (Exception e)               { return 503; }

// BookController - so the load balancer becomes visible
dto.setServedBy(Integer.parseInt(environment.getProperty("server.port")));
```

## Points to make during the session

1. **One line for one line.** Put the 3-tier version on screen beside this one:
   `bookRepository.findById(id)` became `bookClient.getBook(id)`. Everything else
   in `TransactionController` — both catch blocks, the timeouts — exists only
   because the second line crosses a network. That is the honest summary of what
   microservices cost.

2. **Nobody implements `BookClient`.** Ask where the HTTP code is. There is none:
   Feign builds the class at startup from the annotations. Students who have just
   met MapStruct will recognise the idea — you declare, the tool writes.

3. **A name, not an address.** `"library-book-service"` is the other service's
   `spring.application.name`. In step 6 that same name points at two ports at
   once and nothing in transaction-service changes. If a student hardcodes a URL,
   step 6 simply cannot work — that is the cheapest way to make the naming server
   feel necessary.

4. **A 404 is not an empty `Optional` any more.** Feign throws. This catches
   people out, and it is a good example of a leaky abstraction: the call *looks*
   like a method call, but it fails like a network request.

5. **400 and 503 mean different things.** The book not existing is the caller's
   mistake. book-service being down is not. Students often collapse both into
   500; the distinction is worth the two minutes.

6. **The uncomfortable question.** After step 5b, ask: we have made the
   application *less* reliable than the 3-tier version, so what did we buy? Step 6
   is half the answer (scale one part alone). The other half is organisational —
   separate teams, separate deployments — and it does not show up on a laptop.

7. **`BookDTO` twice, on purpose.** See the note at the end of the starting
   repo's README. Sharing a jar re-couples the two services; duplicating costs
   discipline. There is no free answer, and saying so is more useful than
   pretending there is.

8. **What we left out.** No gateway, no circuit breaker, no message queue, no
   tracing, no config server. Each solves a real problem — after these six steps,
   not before.

## Mistakes students often make

| What they do | What happens |
| --- | --- |
| Forget `@EnableFeignClients` | `No qualifying bean of type 'th.mfu.transaction.BookClient'` |
| `@FeignClient(url = "http://localhost:8080")` | Works until step 6, then cannot load balance |
| Reload the dashboard immediately | Empty list. Eureka needs ~30 seconds |
| Start transaction-service before the naming server | Log full of connection errors until the server appears |
| Catch `Exception` before `FeignException.NotFound` | Everything becomes 503 and the 400 case is unreachable |
| Return the port of their *own* service in `servedBy` | It never changes, so the load balancer looks broken |
| Run only one copy of book-service in step 6 | `servedBy` never changes |
| Expect the transaction list to survive a restart | H2 is in memory. This is correct behaviour |
| Edit the test files | The tests are the specification |
