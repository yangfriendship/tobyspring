package springbook.user.sqlservice.reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;
import springbook.user.sqlservice.repository.SqlRepository;

public class XmlSqlReader implements SqlReader {

    private static final String DEFAULT_FILE_PATH = "/sqlmap/sqlmap.xml";
    private String sqlmapFile = DEFAULT_FILE_PATH;

    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;
    }

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
