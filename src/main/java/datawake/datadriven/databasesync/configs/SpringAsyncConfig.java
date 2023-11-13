package datawake.datadriven.databasesync.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class SpringAsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean(name = "threadPoolExecutor")
    public Executor threadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean(name = "threadPoolDatabaseSyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(500);
        executor.setQueueCapacity(250);
        executor.setThreadNamePrefix("DatabaseAsyncExecutor-");
        executor.initialize();
        return executor;
    }
}