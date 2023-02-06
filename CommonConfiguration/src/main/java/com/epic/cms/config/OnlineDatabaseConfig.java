package com.epic.cms.config;

import com.epic.cms.util.CommonOnlineDbVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/*@Configuration
@EnableTransactionManagement*/
public class OnlineDatabaseConfig {

    @Autowired
    CommonOnlineDbVarList commonOnlineDbVarList;

    @Bean(name = "onlineDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.online")
    public DataSource onlineDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl(commonOnlineDbVarList.getOnlineUrl());
        dataSource.setUsername(commonOnlineDbVarList.getOnlineUsername());
        dataSource.setPassword(commonOnlineDbVarList.getOnlinePassword());
        return dataSource;
        // return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate onlineJdbcTemplate(@Qualifier("onlineDataSource") DataSource ds2) {
        return new JdbcTemplate(ds2);
    }

    @Bean(name ="onlineDb")
    public PlatformTransactionManager platformTransactionManager(@Qualifier("onlineDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    @Bean(name = "onlineDb")
    DataSourceTransactionManager onlineDb(@Qualifier("onlineDataSource") DataSource datasource) {
        DataSourceTransactionManager txm = new DataSourceTransactionManager(datasource);
        return txm;
    }
}
