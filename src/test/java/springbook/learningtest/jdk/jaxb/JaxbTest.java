package springbook.learningtest.jdk.jaxb;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.junit.Assert;
import org.junit.Test;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

public class JaxbTest {

    @Test
    public void unmarshallerSqlMapTest() throws IOException, JAXBException, URISyntaxException {
        StreamSource xmlSource = new StreamSource(
            getClass().getResourceAsStream("/sqlmap/sqlmap.xml")
        );

        String contextPath = Sqlmap.class.getPackage().getName();
        JAXBContext context = JAXBContext.newInstance(contextPath);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Sqlmap sqlmap = (Sqlmap) unmarshaller
            .unmarshal(getClass().getResourceAsStream("/sqlmap/sqlmap.xml"));

        List<SqlType> sql = sqlmap.getSql();

        Assert.assertEquals(6,sql.size());
        Assert.assertEquals(sql.get(0).getKey(),"userAdd");
        Assert.assertEquals(sql.get(1).getKey(),"userGet");
        Assert.assertEquals(sql.get(2).getKey(),"userDeleteAll");
        Assert.assertEquals(sql.get(3).getKey(),"userGetCount");
        Assert.assertEquals(sql.get(4).getKey(),"userGetAll");
        Assert.assertEquals(sql.get(5).getKey(),"userUpdate");
    }


}
