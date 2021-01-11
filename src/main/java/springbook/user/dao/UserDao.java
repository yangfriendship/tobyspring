package springbook.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import springbook.user.User;

public interface UserDao {

    public void add(User user) ;

    public void addAll(User... users);

    public void addAll(List<User> users);

    public User get(String id);

    public void deleteAll()  ;

    public int getCount() ;

    public List<User> getAll();

    void update(User user1);
}
