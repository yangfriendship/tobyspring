package springbook.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import springbook.user.sqlservice.OxmSqlService;
import springbook.user.sqlservice.SqlService;
import springbook.user.sqlservice.repository.EmbeddedDbSqlRegistry;
import springbook.user.sqlservice.repository.SqlRegistry;

@Configuration
@ComponentScan("springbook")
public class SqlServiceContext {

    @Autowired
    private SqlMapConfig sqlMapConfig;

    @Bean
    public DataSource embeddedDatabase() {
        return new EmbeddedDatabaseBuilder()
            .setName("embeddedDatabase")
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("sqlmap/sqlmapSchema.sql")
            .build();
    }

    @Bean
    public SqlRegistry sqlRegistry() {
        EmbeddedDbSqlRegistry registry = new EmbeddedDbSqlRegistry();
        registry.setDataSource(embeddedDatabase());
        return registry;
    }

    @Bean
    public SqlService sqlService() {
        OxmSqlService sqlService = new OxmSqlService();
        sqlService.setUnmarshaller(unmarshaller());
        sqlService.setSqlRepository(sqlRegistry());
        sqlService.setSqlmapFile(this.sqlMapConfig.getSqlMapResource());
        return sqlService;
    }

    @Bean
    public Unmarshaller unmarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("springbook.user.sqlservice.jaxb");
        return marshaller;
    }
}
