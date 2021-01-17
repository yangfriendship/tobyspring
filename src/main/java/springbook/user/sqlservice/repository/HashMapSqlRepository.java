package springbook.user.sqlservice.repository;

import java.util.HashMap;
import java.util.Map;
import springbook.user.exception.SqlNotFountException;
import springbook.user.exception.SqlRetrievalFailureException;

public class HashMapSqlRepository implements SqlRegistry {

    private Map<String, String> sqlMap = new HashMap<String, String>();

    @Override
    public void registerSql(String key, String sql) {
        this.sqlMap.put(key,sql);
    }

    @Override
    public String findSql(String key) throws SqlNotFountException {
        if (!sqlMap.containsKey(key)) {
            throw new SqlRetrievalFailureException(key + ERROR_MESSAGE);
        }
        return sqlMap.get(key);
    }
}
