package springbook.user.sqlservice.repository;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import springbook.user.exception.SqlNotFountException;
import springbook.user.exception.SqlUpdateFailureException;

public class ConcurrentHashMapSqlRegistry implements UpdateTableSqlRegistry {

    private Map<String, String> sqlmap = new ConcurrentHashMap<String, String>();

    @Override
    public void registerSql(String key, String sql) {
        sqlmap.put(key, sql);
    }

    @Override
    public String findSql(String key) throws SqlNotFountException {
        if (!this.sqlmap.containsKey(key)) {
            throw new SqlNotFountException(key + "를 이용해서 Sql을 찾을 수 없습니다.");
        }
        return this.sqlmap.get(key);
    }

    @Override
    public void updateSql(String key, String sql) throws SqlUpdateFailureException {
        if (!this.sqlmap.containsKey(key)) {
            throw new SqlUpdateFailureException(key + "를 이용해서 Sql을 찾을 수 없습니다.");
        }
        this.sqlmap.put(key, sql);
    }

    @Override
    public void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailureException {
        for (Entry<String, String> sql : sqlmap.entrySet()) {
            updateSql(sql.getKey(), sql.getValue());
        }
    }
}
