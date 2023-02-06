package com.epic.cms.config;

import com.epic.cms.util.CommonBackendDbVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/*@Configuration
@ComponentScan(basePackages = {"com.epic.cms.*"})
@EnableTransactionManagement*/
public class BackendDatabaseConfig {

    @Autowired
    CommonBackendDbVarList commonBackendDbVarList;

    @Primary
    @Bean(name = "backendDataSource")
    public DataSource backendDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        System.out.println(commonBackendDbVarList.getBackendUrl());
//        dataSource.setUrl("jdbc:oracle:thin:@172.30.35.11:1552:tstcmsbk");
//        dataSource.setUsername("DFCCBACKENDMVISAORTEST6");
//        dataSource.setPassword("DFCCBACKENDMVISAORTEST6");
        dataSource.setUrl(commonBackendDbVarList.getBackendUrl());
        dataSource.setUsername(commonBackendDbVarList.getBackendUsername());
        dataSource.setPassword(commonBackendDbVarList.getBackendPassword());
        return dataSource;
        // return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate backendJdbcTemplate(@Qualifier("backendDataSource") DataSource ds1) {
        return new JdbcTemplate(ds1);
    }

    @Bean(name ="backendDb")
    public PlatformTransactionManager platformTransactionManager(@Qualifier("backendDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    @Bean(name = "backendDb")
    @Autowired
    DataSourceTransactionManager backendDb(@Qualifier("backendDataSource") DataSource datasource) {
        DataSourceTransactionManager txm = new DataSourceTransactionManager(datasource);
        return txm;
    }
}
