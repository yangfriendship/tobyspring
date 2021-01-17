package springbook.user.sqlservice.repository;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import springbook.user.exception.SqlUpdateFailureException;

public class EmbeddedSqlRegistryTest extends AbstractSqlRegistryTest {

    private EmbeddedDatabase database;

    @Override
    protected UpdateTableSqlRegistry createSqlRegistry() {

        database = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("/sqlmap/sqlmapSchema.sql")
            .build();

        EmbeddedDbSqlRegistry registry = new EmbeddedDbSqlRegistry();
        registry.setDataSource(database);
        return registry;
    }

    @Test
    public void transactionTest() {
        super.sqlmap.put("KEY1", "CHANGED1");
        super.sqlmap.put("KEY2", "CHANGED2");
        super.sqlRegistry.updateSql(super.sqlmap);
        String findKey = sqlRegistry.findSql("KEY1");
        Assert.assertEquals("CHANGED1", findKey);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    class TestUpdateTableSqlRegistry extends EmbeddedDbSqlRegistry {

        private final String DEFAULT_TARGET_KEY = "KEY1";
        private String EXCEPTION_TARGET_KEY = DEFAULT_TARGET_KEY;

        public void setEXCEPTION_TARGET_KEY(String EXCEPTION_TARGET_KEY) {
            this.EXCEPTION_TARGET_KEY = EXCEPTION_TARGET_KEY;
        }

        @Override
        public void updateSql(String key, String sql) throws SqlUpdateFailureException {
            if (key.equals(EXCEPTION_TARGET_KEY)) {
                throw new RuntimeException();
            }
            super.updateSql(key, sql);
        }
    }
}
