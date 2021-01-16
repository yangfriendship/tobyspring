package springbook.user.sqlservice;

import springbook.user.exception.SqlNotFountException;

public interface SqlRepository {

    void registerSql(String key, String sql);

    String findSql(String key) throws SqlNotFountException;

}
