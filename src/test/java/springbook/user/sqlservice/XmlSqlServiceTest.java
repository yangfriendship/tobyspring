package springbook.user.sqlservice;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class XmlSqlServiceTest {

    @Test
    public void xmlLoadTest(){
        XmlSqlService sqlService = new XmlSqlService();
        sqlService.setSqlmapFile("/sqlmap/sqlmap.xml");
        sqlService.setSqlReader(sqlService);
        sqlService.setSqlRepository(sqlService);

        sqlService.load();
        String userAdd = sqlService.getSql("userAdd");
    }

    @Test
    public void defaultSqlServiceTest(){
        DefaultSqlService defaultSqlService = new DefaultSqlService();
        defaultSqlService.load();
        defaultSqlService.getSql("userDeleteAll");

    }

}