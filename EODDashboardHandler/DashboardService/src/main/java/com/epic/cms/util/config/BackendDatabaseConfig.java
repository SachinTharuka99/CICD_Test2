///**
// * Author : rasintha_j
// * Date : 3/10/2023
// * Time : 6:34 PM
// * Project Name : ecms_eod_product
// */
//
//package com.epic.cms.util.config;
//
//import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.persistence.EntityManagerFactory;
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//        basePackages = {"com.epic.cms.*"},
//        entityManagerFactoryRef = "backendEM",
//        transactionManagerRef = "chainedTransactionManager"
//)
//public class BackendDatabaseConfig {
//    private Map<String,Object> jpaProperties() {
//        Map<String,Object> jpaProperties = new HashMap<>();
//        jpaProperties.put("spring.jpa.show-sql", true);
//        jpaProperties.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
//        return jpaProperties;
//    }
//
//    @Primary
//    @Bean(name="backednDS")
//    @ConfigurationProperties(prefix = "spring.datasource.backend")
//    public DataSource backendDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Primary
//    @Bean(name="backendEM")
//    public LocalContainerEntityManagerFactoryBean backendEntityManagerFactory(final EntityManagerFactoryBuilder builder)
//    {
//        return builder
//                .dataSource(backendDataSource())
//                .packages("com.epic.cms.api.card.dao.backend","com.epic.cms.api.audit.entity")
//                .persistenceUnit("backendDB")
//                .properties(jpaProperties())
//                .build();
//    }
//
//    @Bean(name="backendTM")
//    @Autowired
//    public PlatformTransactionManager transactionManager(@Qualifier("backendEM") EntityManagerFactory emf) {
//        JpaTransactionManager txManager = new JpaTransactionManager();
//        txManager.setEntityManagerFactory(emf);
//        return txManager;
//    }
//}
