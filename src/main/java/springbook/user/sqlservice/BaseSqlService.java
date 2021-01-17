package springbook.user.sqlservice;

import javax.annotation.PostConstruct;
import springbook.user.exception.SqlNotFountException;
import springbook.user.exception.SqlRetrievalFailureException;
import springbook.user.sqlservice.reader.SqlReader;
import springbook.user.sqlservice.repository.SqlSqlRegistry;

public class BaseSqlService implements SqlService {

    private SqlReader sqlReader;
    private SqlSqlRegistry sqlRepository;

    public void setSqlReader(SqlReader sqlReader) {
        this.sqlReader = sqlReader;
    }

    public void setSqlRepository(SqlSqlRegistry sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @PostConstruct
    public void load() {
        this.sqlReader.read(sqlRepository);
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {

        try {
            return this.sqlRepository.findSql(key);
        } catch (SqlNotFountException e) {
            throw e;
        }

    }
}
