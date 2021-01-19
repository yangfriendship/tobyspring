package springbook.config;

import java.sql.Driver;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springbook.config.annotation.EnableSqlService;

@Configuration
@ComponentScan(basePackages = "springbook.user")
@EnableTransactionManagement
@Import({TestAppConfig.class, ProductionAppContext.class})
@PropertySource("/database.properties")
@EnableSqlService
public class AppContext implements SqlMapConfig {

    @Value("${db.url}")
    private String url;
    @Value("${db.driverClass}")
    private Class<? extends Driver> driverClass;
    @Value("${db.username}")
    private String userName;
    @Value("${db.password}")
    private String password;


    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setUrl(this.url);
        dataSource.setDriverClass(this.driverClass);
        dataSource.setUsername(this.userName);
        dataSource.setPassword(this.password);
        return dataSource;
    }


    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource());
        return transactionManager;
    }


    @Override
    public Resource getSqlMapResource() {
        System.out.println("okok");
        return new ClassPathResource("sqlmap/sqlmap.xml");
    }
}
