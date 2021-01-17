package springbook.user.sqlservice.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import springbook.user.exception.SqlNotFountException;
import springbook.user.exception.SqlUpdateFailureException;

public abstract class AbstractSqlRegistryTest {

    protected UpdateTableSqlRegistry sqlRegistry;
    protected Map<String, String> sqlmap;

    @Before
    public void setUp() {

        sqlmap = new HashMap<String, String>();
        sqlmap.put("KEY1", "SQL1");
        sqlmap.put("KEY2", "SQL2");
        sqlmap.put("KEY3", "SQL3");
        sqlRegistry = createSqlRegistry();

        for(Entry<String,String> sql : sqlmap.entrySet()){
            sqlRegistry.registerSql(sql.getKey(),sql.getValue());
        }

    }

    protected abstract UpdateTableSqlRegistry createSqlRegistry();

    protected void checkFindResult(String expected1, String expected2, String expected3) {
        Assert.assertEquals(expected1, sqlRegistry.findSql("KEY1"));
        Assert.assertEquals(expected2, sqlRegistry.findSql("KEY2"));
        Assert.assertEquals(expected3, sqlRegistry.findSql("KEY3"));
    }

    @Test(expected = SqlNotFountException.class)
    public void unknownKey() {
        sqlRegistry.findSql("UNKNOWN_KEY");
    }

    @Test
    public void updateSingle() {
        sqlRegistry.updateSql("KEY2", "Modified2");
        checkFindResult("SQL1", "Modified2", "SQL3");
    }

    @Test(expected = SqlUpdateFailureException.class)
    public void updateWithNotExistingKey() {
        sqlRegistry.updateSql("UNKNOWN_KEY", "Modified2");
    }
}