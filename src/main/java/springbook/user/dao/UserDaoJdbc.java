package springbook.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import springbook.user.Level;
import springbook.user.User;
import springbook.user.sqlservice.SqlService;

@Repository
public class UserDaoJdbc implements UserDao {

    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SqlService sqlService;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void add(final User user) throws DuplicateKeyException {
        this.jdbcTemplate.update(
            this.sqlService.getSql("userAdd")
            , user.getId(), user.getName(), user.getPassword(), user.getLevel().intValue(),
            user.getLogin(), user.getRecommend(), user.getEmail());
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
        return this.jdbcTemplate.queryForObject(this.sqlService.getSql("userGet"), new Object[]{id}
            , userRowMapper()
        );
    }

    public void deleteAll() {
        this.jdbcTemplate.update(this.sqlService.getSql("userDeleteAll"));
    }

    public int getCount() {
        return this.jdbcTemplate.queryForInt(this.sqlService.getSql("userGetCount"));
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
            .query(this.sqlService.getSql("userGetAll"), userRowMapper());
    }

    @Override
    public void update(User user) {
        this.jdbcTemplate.update(this.sqlService.getSql("userUpdate")
            , user.getName(), user.getPassword(), user.getLevel().intValue(),
            user.getLogin(), user.getRecommend(), user.getEmail(), user.getId());
    }
}
