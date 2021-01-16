package springbook.user.sqlservice;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import org.springframework.oxm.Unmarshaller;
import springbook.user.exception.SqlNotFountException;
import springbook.user.exception.SqlRetrievalFailureException;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;
import springbook.user.sqlservice.reader.SqlReader;
import springbook.user.sqlservice.repository.HashMapSqlRepository;
import springbook.user.sqlservice.repository.SqlRepository;

public class OxmSqlService implements SqlService {

    private final OxmSqlReader oxmSqlReader = new OxmSqlReader();
    private SqlRepository sqlRepository = new HashMapSqlRepository();

    public void setSqlRepository(SqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        oxmSqlReader.setUnmarshaller(unmarshaller);
    }

    public void setSqlmapFile(String sqlmapFile) {
        oxmSqlReader.setSqlmapFile(sqlmapFile);
    }

    @PostConstruct
    public void load() {
        this.oxmSqlReader.read(this.sqlRepository);
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        try {
            return this.sqlRepository.findSql(key);
        } catch (SqlNotFountException e) {
            throw e;
        }
    }

    private class OxmSqlReader implements SqlReader {

        private static final String DEFAULT_PATH = "/sqlmap/sqlmap.xml";

        private Unmarshaller unmarshaller;
        private String sqlmapFile = DEFAULT_PATH;

        public void setUnmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
        }

        public void setSqlmapFile(String sqlmapFile) {
            this.sqlmapFile = sqlmapFile;
        }

        @Override
        public void read(SqlRepository sqlRepository) {
            StreamSource source = new StreamSource(
                getClass().getResourceAsStream(this.sqlmapFile));
            try {
                Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(source);
                for (SqlType sqlType : sqlmap.getSql()) {
                    sqlRepository.registerSql(sqlType.getKey(), sqlType.getValue());
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(this.sqlmapFile +
                    "을 가져올 수 없습니다.");
            }
        }
    }
}
