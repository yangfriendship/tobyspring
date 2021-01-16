package springbook.user.sqlservice;

import springbook.user.sqlservice.reader.XmlSqlReader;
import springbook.user.sqlservice.repository.HashMapSqlRepository;

public class DefaultSqlService extends BaseSqlService {

    public DefaultSqlService() {
        setSqlReader(new XmlSqlReader());
        setSqlRepository(new HashMapSqlRepository());
    }
}
