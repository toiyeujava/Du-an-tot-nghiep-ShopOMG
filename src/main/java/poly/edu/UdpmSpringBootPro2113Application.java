package poly.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class UdpmSpringBootPro2113Application {

	@PostConstruct
	void started() {
		// Set JVM default timezone to Vietnam time to ensure consistenty
		// between local and server environments
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}

	public static void main(String[] args) {
		SpringApplication.run(UdpmSpringBootPro2113Application.class, args);
	}

}
