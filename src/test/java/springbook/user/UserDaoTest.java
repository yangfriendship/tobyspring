package springbook.user;

import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserDaoTest {

    private UserDao userDao;

    @Before
    public void setUp() {
        this.userDao = new DaoFactory().userDao();
    }

    @Test
    public void addTest() throws SQLException, ClassNotFoundException {

        User user = new User("1", "name", "ps");
        userDao.add(user);

        User find = userDao.get(user.getId());

        Assert.assertEquals(user.getId(), find.getId());
        Assert.assertEquals(user.getName(), find.getName());
        Assert.assertEquals(user.getPassword(), find.getPassword());

    }

}