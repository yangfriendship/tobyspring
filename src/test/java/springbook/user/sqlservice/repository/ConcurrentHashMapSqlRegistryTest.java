package springbook.user.sqlservice.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import springbook.user.exception.SqlNotFountException;
import springbook.user.exception.SqlUpdateFailureException;

public class ConcurrentHashMapSqlRegistryTest {

    private UpdateTableSqlRegistry sqlRegistry;

    @Before
    public void setUp() {
        sqlRegistry = new ConcurrentHashMapSqlRegistry();
        sqlRegistry.registerSql("KEY1", "SQL1");
        sqlRegistry.registerSql("KEY2", "SQL2");
        sqlRegistry.registerSql("KEY3", "SQL3");
    }

    private void checkFindResult(String expected1, String expected2, String expected3) {
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
    public void updateWithNotExistingKey(){
        sqlRegistry.updateSql("UNKNOWN_KEY","Modified2");
    }
}