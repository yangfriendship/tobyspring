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
import springbook.user.sqlservice.reader.SqlReader;
import springbook.user.sqlservice.repository.SqlSqlRegistry;

public class XmlSqlService implements SqlService, SqlSqlRegistry, SqlReader {


    private Map<String, String> sqlMap = new HashMap<String, String>();
    private String sqlmapFile;
    private SqlSqlRegistry sqlRepository;
    private SqlReader sqlReader;

    public XmlSqlService() {

    }

    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;
    }

    public void setSqlRepository(SqlSqlRegistry sqlRepository) {
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
    public void read(SqlSqlRegistry sqlRepository) {
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
