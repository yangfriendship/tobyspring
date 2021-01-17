package springbook.user.sqlservice.repository;

public class ConcurrentHashMapSqlRegistryTest extends AbstractSqlRegistryTest {

    @Override
    protected UpdateTableSqlRegistry createSqlRegistry() {
        return new ConcurrentHashMapSqlRegistry();
    }
}
