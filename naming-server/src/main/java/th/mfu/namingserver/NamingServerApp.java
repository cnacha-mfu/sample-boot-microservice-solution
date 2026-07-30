package th.mfu.namingserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * The naming server, on port 8761.
 * <p>
 * It keeps a list: which services are running, and at which address. Services
 * put themselves on the list; other services read it.
 * <p>
 * This module is finished. Nothing to do here - open http://localhost:8761/ once
 * it is running.
 */
@SpringBootApplication
@EnableEurekaServer
public class NamingServerApp {

    public static void main(String[] args) {
        SpringApplication.run(NamingServerApp.class, args);
    }
}
