package springbook.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import springbook.user.strategy.JdbcContext;
import springbook.user.strategy.StatementStrategy;

public class UserDao {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private JdbcContext jdbcContext;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcContext = new JdbcContext(dataSource);
    }

    public void add(final User user) throws ClassNotFoundException, SQLException {
        this.jdbcTemplate.update("insert into users(id,name,password ) values(?,?,?) "
            , user.getId(), user.getName(), user.getPassword());
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        return this.jdbcTemplate.queryForObject("select * from users where id= ?", new Object[]{id}
            , userRowMapper()
        );
    }

    public void deleteAll() throws SQLException {
        this.jdbcTemplate.update("delete from users");
    }

    public int getCount() throws SQLException {
        return this.jdbcTemplate.queryForInt("select count(*) from users");
    }

    private RowMapper<User> userRowMapper(){
        return new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet resultSet, int i) throws SQLException {
                User user = new User();
                user.setId(resultSet.getString("id"));
                user.setName(resultSet.getString("name"));
                user.setPassword(resultSet.getString("password"));
                return user;
            }
        };
    }

    public List<User> getAll() {
        return this.jdbcTemplate
            .query("select * from users order by id", userRowMapper());
    }
}
