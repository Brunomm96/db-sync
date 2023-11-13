package datawake.datadriven.databasesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude= {UserDetailsServiceAutoConfiguration.class})
@EnableScheduling
@EnableJpaAuditing
@EnableAsync
public class DatabaseSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatabaseSyncApplication.class, args);
		System.out.println("\n		SERVER ONLINE\n");
	}

}
