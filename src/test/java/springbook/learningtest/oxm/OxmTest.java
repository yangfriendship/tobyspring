package springbook.learningtest.oxm;

import java.io.IOException;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springbook.config.AppContext;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppContext.class)
@ActiveProfiles("test")
public class OxmTest {

    @Autowired
    Unmarshaller unmarshaller;

    @Test
    public void unmarshallerTest() throws IOException {

        StreamSource streamSource = new StreamSource(
            getClass().getResourceAsStream("/sqlmap/sqlmap.xml"));

        Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(streamSource);

        List<SqlType> sqls = sqlmap.getSql();

        Assert.assertEquals("userAdd",sqls.get(0).getKey());
    }
}
