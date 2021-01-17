package springbook.user.sqlservice.repository;

import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import springbook.user.exception.SqlNotFountException;
import springbook.user.exception.SqlUpdateFailureException;

public class EmbeddedDbSqlRegistry implements UpdateTableSqlRegistry {

    private SimpleJdbcTemplate template;
    private TransactionTemplate transactionTemplate;

    public void setDataSource(DataSource dataSource) {
        this.template = new SimpleJdbcTemplate(dataSource);
        this.transactionTemplate = new TransactionTemplate(
            new DataSourceTransactionManager(dataSource));
    }

    @Override
    public void updateSql(String key, String sql) throws SqlUpdateFailureException {
        int updated = this.template.update("update sqlmap set sql_ =? where key_ = ?", sql, key);
        if (updated == 0) {
            throw new SqlUpdateFailureException(key + "에 해당하는 Sql을 찾을 수 없습니다.");
        }
    }

    // 트랜잭션이 필요한 로직
    @Override
    public void updateSql(final Map<String, String> sqlmap) throws SqlUpdateFailureException {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                for (Entry<String, String> entry : sqlmap.entrySet()) {
                    updateSql(entry.getKey(), entry.getValue());
                }
            }
        });
    }

    @Override
    public void registerSql(String key, String sql) {
        this.template.update("insert into sqlmap(key_, sql_) values(?,?)", key, sql);
    }

    @Override
    public String findSql(String key) throws SqlNotFountException {
        try {
            return this.template
                .queryForObject("select sql_ from sqlmap where key_ = ?", String.class, key);
        } catch (EmptyResultDataAccessException e) {
            throw new SqlNotFountException(key);
        }
    }
}
