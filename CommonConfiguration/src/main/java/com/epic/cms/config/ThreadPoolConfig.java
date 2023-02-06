package com.epic.cms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Primary
    @Bean(name = "ThreadPool_01")
    public ThreadPoolTaskExecutor taskExecutor01() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("ThreadPool_01");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ThreadPool_50")
    public ThreadPoolTaskExecutor taskExecutor1() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("ThreadPool_03");
        executor.initialize();
        return executor;
    }

    @Bean(name = "taskExecutor2")
    public ThreadPoolTaskExecutor taskExecutor2() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(75);
        executor.setMaxPoolSize(75);
        executor.setQueueCapacity(2000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("ThreadPool_75");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ThreadPool_100")
    public ThreadPoolTaskExecutor taskExecutor3() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(1000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("ThreadPool_100");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ThreadPool_05")
    public ThreadPoolTaskExecutor taskExecutor05() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("ThreadPool_05");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ThreadPool_FileHandler")
    public ThreadPoolTaskExecutor fileHandler() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("ThreadPool_FileHandler");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ThreadPool_FileReader")
    public ThreadPoolTaskExecutor fileReader() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(100000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("ThreadPool_FileReader");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ThreadPool_FileValidator")
    public ThreadPoolTaskExecutor fileValidator() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("ThreadPool_FileValidator");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ThreadPool_ATMFileValidator")
    public ThreadPoolTaskExecutor fileValidator1() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("TP_ATM");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ThreadPool_PaymentFileValidator")
    public ThreadPoolTaskExecutor fileValidator2() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("TP_PAYMENT");
        executor.initialize();
        return executor;
    }
}
