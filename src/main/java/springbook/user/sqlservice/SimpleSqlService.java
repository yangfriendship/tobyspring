package springbook.user.sqlservice;

import java.util.Map;
import springbook.user.exception.SqlRetrievalFailureException;

public class SimpleSqlService implements SqlService {

    private static final String ERROR_MESSAGE = "에 대한 SQL을 찾을 수 없습니다.";

    private Map<String, String> sqlMap;

    public void setSqlMap(Map<String, String> sqlMap) {
        this.sqlMap = sqlMap;
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {

        if (!this.sqlMap.containsKey(key)) {
            throw new SqlRetrievalFailureException(key + ERROR_MESSAGE);
        }

        return this.sqlMap.get(key);
    }
}
