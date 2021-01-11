package springbook.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public interface UserDao {

    public void add(final User user) ;

    public User get(String id);

    public void deleteAll()  ;

    public int getCount() ;

    public List<User> getAll();

}
