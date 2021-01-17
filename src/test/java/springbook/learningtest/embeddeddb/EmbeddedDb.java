package springbook.learningtest.embeddeddb;

import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class EmbeddedDb {

    private EmbeddedDatabase database;
    private SimpleJdbcTemplate template;

    @Before
    public void setUp(){

        this.database = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("/sqlmap/sqlmapSchema.sql")
            .addScript("/sqlmap/data.sql")
            .build();
        this.template = new SimpleJdbcTemplate(database);
    }

    @Test
    public void initData(){

        Assert.assertEquals(2,template.queryForInt("select count(*) from SQLMAP "));

        List<Map<String, Object>> list = template
            .queryForList("select * from SQLMAP order by key_ ");

        Assert.assertEquals("KEY1",(String)list.get(0).get("KEY_"));
        Assert.assertEquals("KEY2",(String)list.get(1).get("KEY_"));
        Assert.assertEquals("SQL1",(String)list.get(0).get("SQL_"));
        Assert.assertEquals("SQL2",(String)list.get(1).get("SQL_"));
    }

    @After
    public void close(){
        database.shutdown();
    }

}
