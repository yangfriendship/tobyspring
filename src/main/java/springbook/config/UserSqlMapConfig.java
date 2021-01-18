package springbook.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

public class UserSqlMapConfig implements SqlMapConfig {

    @Override
    public Resource getSqlMapResource() {
        return new ClassPathResource("sqlmap/sqlmap.xml");
    }
}
