package springbook.user.sqlservice.repository;

import springbook.user.exception.SqlNotFountException;

public interface SqlRegistry {

    String ERROR_MESSAGE = "에 대한 SQL을 찾을 수 없습니다.";

    void registerSql(String key, String sql);

    String findSql(String key) throws SqlNotFountException;

}
