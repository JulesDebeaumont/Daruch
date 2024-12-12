package daruch;

import daruch.config.StorageConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageConfig.class)
public class DarucheApplication {

	public static void main(String[] args) {
		SpringApplication.run(DarucheApplication.class, args);
	}

}
