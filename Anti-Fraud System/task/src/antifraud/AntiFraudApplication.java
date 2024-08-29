package antifraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This is an annotation that tells Spring Boot that this is the main application class.
// It automatically configures everything needed to run the application.
@SpringBootApplication
public class AntiFraudApplication {
    // The main method is started
    public static void main(String[] args) {
        SpringApplication.run(AntiFraudApplication.class, args);
    }
}
