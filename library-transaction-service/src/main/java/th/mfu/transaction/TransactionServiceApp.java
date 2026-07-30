package th.mfu.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * transaction-service. Port 8100.
 * <p>
 * SOLUTION for step 2 and step 3.
 * <p>
 * Two annotations, two different jobs:
 * <ul>
 * <li>&#64;EnableDiscoveryClient - put me on the naming server's list, and give
 * me a copy of it so I can look others up.</li>
 * <li>&#64;EnableFeignClients - go and find every interface marked
 * &#64;FeignClient and build it. Leave this out and BookClient stays an
 * interface nobody implements, and the app fails to start with "No qualifying
 * bean of type BookClient".</li>
 * </ul>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class TransactionServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApp.class, args);
    }
}
