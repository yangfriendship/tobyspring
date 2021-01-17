package springbook.user.sqlservice;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;
import springbook.user.exception.SqlRetrievalFailureException;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;
import springbook.user.sqlservice.reader.SqlReader;
import springbook.user.sqlservice.repository.HashMapSqlRepository;
import springbook.user.sqlservice.repository.SqlSqlRegistry;

public class OxmSqlService implements SqlService {

    private final BaseSqlService baseSqlService = new BaseSqlService();

    private final OxmSqlReader oxmSqlReader = new OxmSqlReader();
    private SqlSqlRegistry sqlRepository = new HashMapSqlRepository();

    public void setSqlRepository(SqlSqlRegistry sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        oxmSqlReader.setUnmarshaller(unmarshaller);
    }

    public void setSqlmapFile(Resource resource) {
        oxmSqlReader.setSqlmapFile(resource);
    }

    @PostConstruct
    public void load() {
        this.baseSqlService.setSqlReader(this.oxmSqlReader);
        this.baseSqlService.setSqlRepository(this.sqlRepository);

        this.baseSqlService.load();
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        return this.baseSqlService.getSql(key);
    }

    private class OxmSqlReader implements SqlReader {

        private final Resource DEFAULT_PATH = new ClassPathResource("/sqlmap/sqlmap.xml");

        private Unmarshaller unmarshaller;
        private Resource sqlmap = DEFAULT_PATH;

        public void setUnmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
        }

        public void setSqlmapFile(Resource sqlmap) {
            this.sqlmap = sqlmap;
        }

        @Override
        public void read(SqlSqlRegistry sqlRepository) {
            try {
                StreamSource source = new StreamSource(sqlmap.getInputStream());
                Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(source);
                for (SqlType sqlType : sqlmap.getSql()) {
                    sqlRepository.registerSql(sqlType.getKey(), sqlType.getValue());
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(this.sqlmap.getFilename() +
                    "을 가져올 수 없습니다.");
            }
        }
    }
}
