package springbook.user.sqlservice;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import springbook.user.exception.SqlNotFountException;
import springbook.user.exception.SqlRetrievalFailureException;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

public class XmlSqlService implements SqlService, SqlRepository, SqlReader {

    private static final String ERROR_MESSAGE = "에 대한 SQL을 찾을 수 없습니다.";

    private Map<String, String> sqlMap = new HashMap<String, String>();
    private String sqlmapFile;
    private SqlRepository sqlRepository;
    private SqlReader sqlReader;

    public XmlSqlService() {

    }

    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;
    }

    public void setSqlRepository(SqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    public void setSqlReader(SqlReader sqlReader) {
        this.sqlReader = sqlReader;
    }

    @PostConstruct
    public void load() {
        this.sqlReader.read(this.sqlRepository);
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        try {
            return this.sqlRepository.findSql(key);
        } catch (SqlNotFountException e) {
            throw e;
        }
    }

    //SqlRegistry
    @Override
    public void registerSql(String key, String sql) {
        sqlMap.put(key, sql);
    }

    @Override
    public String findSql(String key) throws SqlNotFountException {
        if (!sqlMap.containsKey(key)) {
            throw new SqlRetrievalFailureException(key + ERROR_MESSAGE);
        }

        return sqlMap.get(key);

    }

    //SqlReader
    @Override
    public void read(SqlRepository sqlRepository) {
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Sqlmap sqlmap = (Sqlmap) unmarshaller
                .unmarshal(getClass().getResourceAsStream(sqlmapFile));
            for (SqlType sqlType : sqlmap.getSql()) {
                sqlRepository.registerSql(sqlType.getKey(), sqlType.getValue());
            }

        } catch (JAXBException e) {
            throw new RuntimeException();
        }
    }
}
