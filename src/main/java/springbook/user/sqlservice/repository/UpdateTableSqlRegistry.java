package springbook.user.sqlservice.repository;

import java.util.Map;
import springbook.user.exception.SqlUpdateFailureException;

public interface UpdateTableSqlRegistry extends SqlSqlRegistry {

    void updateSql(String key, String sql) throws SqlUpdateFailureException;

    void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailureException;


}
