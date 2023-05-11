/**
 * Author : lahiru_p
 * Date : 12/22/2022
 * Time : 10:49 PM
 * Project Name : ecms_eod_engine - Copy
 */

package com.epic.cms.config;

import com.epic.cms.util.CommonBackendDbVarList;
import com.epic.cms.util.CommonOnlineDbVarList;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
@Configuration
@ComponentScan(basePackages = {"com.epic.cms.*"})
@EnableTransactionManagement
public class DatabaseConfigChained {

    @Autowired
    CommonBackendDbVarList commonBackendDbVarList;

    @Autowired
    CommonOnlineDbVarList commonOnlineDbVarList;

    @Primary
    @Bean(name = "backendDataSource")
    public DataSource backendDataSource() {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("oracle.jdbc.OracleDriver");
        hikariConfig.setJdbcUrl(commonBackendDbVarList.getBackendUrl());
        hikariConfig.setUsername(commonBackendDbVarList.getBackendUsername());
        hikariConfig.setPassword(commonBackendDbVarList.getBackendPassword());
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setMinimumIdle(10);
        //hikariConfig.setIdleTimeout(200000);
        hikariConfig.setConnectionTimeout(100000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.addDataSourceProperty("socketTimeout", "120");
        hikariConfig.addDataSourceProperty("oracle.jdbc.javaNetNio", "false");
        return new HikariDataSource(hikariConfig);
    }
    @Primary
    @Bean
    public JdbcTemplate backendJdbcTemplate(@Qualifier("backendDataSource") DataSource ds1) {
        return new JdbcTemplate(ds1);
    }

    @Bean(name ="backendDb")
    public PlatformTransactionManager backendDbTransactionManager(@Qualifier("backendDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "onlineDataSource")
    public DataSource onlineDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("oracle.jdbc.OracleDriver");
        hikariConfig.setJdbcUrl(commonOnlineDbVarList.getOnlineUrl());
        hikariConfig.setUsername(commonOnlineDbVarList.getOnlineUsername());
        hikariConfig.setPassword(commonOnlineDbVarList.getOnlinePassword());
        hikariConfig.setMaximumPoolSize(10);
        //hikariConfig.setIdleTimeout(200000);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setConnectionTimeout(100000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.addDataSourceProperty("socketTimeout", "120");
        hikariConfig.addDataSourceProperty("oracle.jdbc.javaNetNio", "false");
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public JdbcTemplate onlineJdbcTemplate(@Qualifier("onlineDataSource") DataSource ds2) {
        return new JdbcTemplate(ds2);
    }

    @Bean(name ="onlineDb")
    public PlatformTransactionManager onlineDbTransactionManager(@Qualifier("onlineDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = "transactionManager")
    public ChainedTransactionManager transactionManager(@Qualifier("backendDb") PlatformTransactionManager backendDb, @Qualifier ("onlineDb") PlatformTransactionManager onlineDb){
        return new ChainedTransactionManager(backendDb,onlineDb);
    }
}
