package springbook.user.sqlservice.reader;

import springbook.user.sqlservice.repository.SqlRegistry;

public interface SqlReader {

    void read(SqlRegistry sqlRepository);

}
