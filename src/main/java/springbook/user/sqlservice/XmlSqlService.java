package springbook.user.sqlservice;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import springbook.user.exception.SqlRetrievalFailureException;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

public class XmlSqlService implements SqlService {

    private static final String DEFAULT_FILE_PATH = "/sqlmap/sqlmap.xml";
    private static final String ERROR_MESSAGE = "에 대한 SQL을 찾을 수 없습니다.";

    private Map<String, String> sqlMap = new HashMap<String, String>();
    private String sqlmapFile = DEFAULT_FILE_PATH;

    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;
    }

    public XmlSqlService() {

    }

    @PostConstruct
    public void load(){
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Sqlmap sqlmap = (Sqlmap) unmarshaller
                .unmarshal(getClass().getResourceAsStream(sqlmapFile));
            for (SqlType sqlType : sqlmap.getSql()) {
                this.sqlMap.put(sqlType.getKey(), sqlType.getValue());
            }

        } catch (JAXBException e) {
            throw new RuntimeException();
        }

    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {

        if (!this.sqlMap.containsKey(key)) {
            throw new SqlRetrievalFailureException(key + ERROR_MESSAGE);
        }

        return this.sqlMap.get(key);
    }
}
