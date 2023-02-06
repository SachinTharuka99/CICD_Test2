/**
 * Author : lahiru_p
 * Date : 12/22/2022
 * Time : 10:47 PM
 * Project Name : ecms_eod_engine - Copy
 */

package com.epic.cms.config;

/*import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.sql.SQLException;
import java.util.Properties;*/

/*@Configuration
@ComponentScan
@EnableTransactionManagement*/
public class DatabaseConfigAtomikos {
/*    @Bean(name = "xa_backend", initMethod = "init", destroyMethod = "close")
    public DataSource AtomikosDataSourceA() throws SQLException {

        AtomikosDataSourceBean bean = new AtomikosDataSourceBean();
        bean.setXaDataSourceClassName("oracle.jdbc.xa.client.OracleXADataSource");
        bean.setUniqueResourceName("xa_backend");
        bean.setXaProperties(xaPropertiesA());
        bean.setMinPoolSize(1);
        bean.setMaxPoolSize(3);
        bean.setMaxIdleTime(60);
        bean.setPoolSize(20);
        return bean;

    }

    @Bean(name = "xa_online", initMethod = "init", destroyMethod = "close")
    public DataSource AtomikosDataSourceB() throws SQLException {

        AtomikosDataSourceBean bean = new AtomikosDataSourceBean();
        bean.setXaDataSourceClassName("oracle.jdbc.xa.client.OracleXADataSource");
        bean.setUniqueResourceName("xa_online");
        bean.setXaProperties(xaPropertiesB());
        bean.setMinPoolSize(1);
        bean.setMaxPoolSize(3);
        bean.setMaxIdleTime(60);
        bean.setPoolSize(10);
        return bean;

    }

    @Bean
    public Properties xaPropertiesA() {
        Properties props = new Properties();
        props.setProperty("URL", "jdbc:oracle:thin:@localhost:1521:XE");
        props.setProperty("user", "DEMO1");
        props.setProperty("password","demo1");
        return props;
    }

    @Bean
    public Properties xaPropertiesB() {
        Properties props = new Properties();
        props.setProperty("URL", "jdbc:oracle:thin:@localhost:1521:XE");
        props.setProperty("user", "DEMO2");
        props.setProperty("password","demo2");
        return props;
    }

    @Bean(name = "backendJdbcTemplate")
    public JdbcTemplate backendJdbcTemplate() throws SQLException {
        JdbcTemplate template = new JdbcTemplate();
        template.setDataSource(AtomikosDataSourceA());
        return template;
    }

    @Bean(name = "onlineJdbcTemplate")
    public JdbcTemplate onlineJdbcTemplate() throws SQLException {
        JdbcTemplate template = new JdbcTemplate();
        template.setDataSource(AtomikosDataSourceB());
        return template;
    }

    @Bean
    public UserTransactionManager userTransactionManager() throws SystemException {
        UserTransactionManager userJta = new UserTransactionManager();
        userJta.setForceShutdown(true);
        userJta.setTransactionTimeout(3000);
        return userJta;
    }

    @Bean
    public UserTransaction userTransactionImp(){
        UserTransactionImp userJtaImpl = new UserTransactionImp();
        try {
            userJtaImpl.setTransactionTimeout(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userJtaImpl;
    }

    @Bean(name="transactionManager")
    public JtaTransactionManager jtaTransactionManager() throws SystemException {
        JtaTransactionManager jta = new JtaTransactionManager();
        jta.setTransactionManagerName("transactionManager");
        jta.setTransactionManager(userTransactionManager());
        jta.setUserTransaction(userTransactionImp());
        jta.setAllowCustomIsolationLevels(true);
        return jta;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }*/
}
