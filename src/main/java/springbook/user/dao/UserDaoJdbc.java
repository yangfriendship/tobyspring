package springbook.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import springbook.user.Level;
import springbook.user.User;

public class UserDaoJdbc implements UserDao {

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void add(final User user) throws DuplicateKeyException {
        this.jdbcTemplate.update(
            "insert into users(id,name,password,level,login,recommend,email ) values(?,?,?,?,?,?,?) "
            , user.getId(), user.getName(), user.getPassword(), user.getLevel().intValue(),
            user.getLogin(), user.getRecommend(),user.getEmail());
    }

    @Override
    public void addAll(User... users) {
        for (User user : users) {
            add(user);
        }
    }

    @Override
    public void addAll(List<User> users) {
        addAll(users.toArray(new User[users.size()]));
    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject("select * from users where id= ?", new Object[]{id}
            , userRowMapper()
        );
    }

    public void deleteAll() {
        this.jdbcTemplate.update("delete from users");
    }

    public int getCount() {
        return this.jdbcTemplate.queryForInt("select count(*) from users");
    }

    private RowMapper<User> userRowMapper() {
        return new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet resultSet, int i) throws SQLException {
                User user = new User();
                user.setId(resultSet.getString("id"));
                user.setName(resultSet.getString("name"));
                user.setPassword(resultSet.getString("password"));
                user.setLevel(Level.valueOf(resultSet.getInt("level")));
                user.setLogin(resultSet.getInt("login"));
                user.setRecommend(resultSet.getInt("recommend"));
                user.setEmail(resultSet.getString("email"));
                return user;
            }
        };
    }

    public List<User> getAll() {
        return this.jdbcTemplate
            .query("select * from users order by id", userRowMapper());
    }

    @Override
    public void update(User user) {
        this.jdbcTemplate.update("update users set "
                + "name=?, password=?, level=?, login=?,recommend=?,email=? where id=?"
            , user.getName(), user.getPassword(), user.getLevel().intValue(),
            user.getLogin(), user.getRecommend(), user.getEmail(),user.getId());
    }
}
