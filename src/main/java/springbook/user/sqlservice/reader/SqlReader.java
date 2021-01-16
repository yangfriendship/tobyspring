package springbook.user.sqlservice.reader;

import springbook.user.sqlservice.repository.SqlRepository;

public interface SqlReader {

    void read(SqlRepository sqlRepository);

}
