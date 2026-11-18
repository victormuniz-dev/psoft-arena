package br.com.psoft.arena.config;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String EXECUTOR_SUBMISSAO = "submissaoExecutor";

    @Bean(name = EXECUTOR_SUBMISSAO)
    public ThreadPoolTaskExecutor submissaoExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("submissao-");
        executor.setTaskDecorator(contextoLogDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);

        return executor;
    }

    private TaskDecorator contextoLogDecorator() {
        return runnable -> {
            Map<String, String> contexto = MDC.getCopyOfContextMap();

            return () -> {
                Map<String, String> anterior = MDC.getCopyOfContextMap();
                aplicarContexto(contexto);

                try {
                    runnable.run();
                } finally {
                    aplicarContexto(anterior);
                }
            };
        };
    }

    private void aplicarContexto(Map<String, String> contexto) {
        if (contexto == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(contexto);
        }
    }
}
