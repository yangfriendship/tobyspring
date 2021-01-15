package springbook.user.sqlservice;

import static org.junit.Assert.*;

import org.junit.Test;

public class XmlSqlServiceTest {

    @Test
    public void xmlLoadTest(){

        XmlSqlService sqlService = new XmlSqlService();
        String userAdd = sqlService.getSql("userAdd");
        System.out.println(userAdd);
    }

}