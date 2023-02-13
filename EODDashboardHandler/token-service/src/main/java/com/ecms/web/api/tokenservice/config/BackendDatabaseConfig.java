package com.ecms.web.api.tokenservice.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "backendEntityManagerFactory",
        transactionManagerRef = "backendTransactionManager",
        basePackages = {"com.ecms.web.api.tokenservice.repository"}
)
public class BackendDatabaseConfig {


    @Bean(name = "backendDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.backend")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "backendEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    barEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("backendDataSource") DataSource dataSource
    ) {
        return
                builder
                        .dataSource(dataSource)
                        .packages("com.ecms.web.api.tokenservice.model.entity")
                        .persistenceUnit("backendDB")
                        .build();
    }

    @Bean(name = "backendTransactionManager")
    public PlatformTransactionManager backendTransactionManager(
            @Qualifier("backendEntityManagerFactory") EntityManagerFactory
                    backendEntityManagerFactory
    ) {
        return new JpaTransactionManager(backendEntityManagerFactory);
    }
}