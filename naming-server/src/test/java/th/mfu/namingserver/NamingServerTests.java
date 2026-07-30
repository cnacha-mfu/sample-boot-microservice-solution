package th.mfu.namingserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

/**
 * The naming server is given to you finished, so these two tests should pass
 * before you write anything.
 * <p>
 * This test class is complete. Do not change it.
 */
@SpringBootTest
public class NamingServerTests {

    @Autowired
    private Environment environment;

    @Test
    public void itIsAEurekaServer() {
        assertNotNull(AnnotationUtils.findAnnotation(
                NamingServerApp.class, EnableEurekaServer.class));
    }

    @Test
    public void itDoesNotRegisterWithItself() {
        assertEquals("8761", environment.getProperty("server.port"));
        assertEquals("false", environment.getProperty("eureka.client.register-with-eureka"));
        assertEquals("false", environment.getProperty("eureka.client.fetch-registry"));
    }
}
