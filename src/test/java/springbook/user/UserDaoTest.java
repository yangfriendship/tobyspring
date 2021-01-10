package springbook.user;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;


public class UserDaoTest {

    private UserDao userDao;
    private ApplicationContext context;

    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp() {
        this.context = new GenericXmlApplicationContext(
            "applicationContext.xml");
        this.userDao = this.context.getBean("userDao", UserDao.class);

        this.user1 = new User("1", "youzheng", "ps1");
        this.user2 = new User("2", "woojung", "ps2");
        this.user3 = new User("3`", "yang", "ps3");
    }

    @After
    public void reset() throws SQLException {
        userDao.deleteAll();
    }

    @Test
    public void addTest() throws SQLException, ClassNotFoundException {
        userDao.add(user1);

        User find = userDao.get(user1.getId());

        Assert.assertEquals(user1.getId(), find.getId());
        Assert.assertEquals(user1.getName(), find.getName());
        Assert.assertEquals(user1.getPassword(), find.getPassword());

    }

    @Test
    public void addTestWithContext() throws SQLException, ClassNotFoundException {
        userDao.add(user1);

        User find = userDao.get(user1.getId());

        Assert.assertEquals(user1.getId(), find.getId());
        Assert.assertEquals(user1.getName(), find.getName());
        Assert.assertEquals(user1.getPassword(), find.getPassword());
    }

    @Test
    public void daoFactorySingleTonTest() {
        DaoFactory factory = new DaoFactory();
        UserDao userDao1 = factory.userDao();
        UserDao userDao2 = factory.userDao();

        Assert.assertNotSame(userDao1, userDao2);
    }

    @Test
    public void appContextSingleTonTest() {
        UserDao userDao1 = context.getBean("userDao", UserDao.class);
        UserDao userDao2 = context.getBean("userDao", UserDao.class);
        Assert.assertSame(userDao1, userDao2);
    }

    @Test
    public void addAndGetTest() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();
        Assert.assertTrue(userDao.getCount() == 0);

        userDao.add(this.user1);
        Assert.assertTrue(userDao.getCount() == 1);

        userDao.add(user2);
        Assert.assertTrue(userDao.getCount() == 2);

        User find = userDao.get(this.user1.getId());
        Assert.assertEquals(this.user1.getId(), find.getId());
        Assert.assertEquals(this.user1.getName(), find.getName());
        Assert.assertEquals(this.user1.getPassword(), find.getPassword());

        User find2 = userDao.get(user2.getId());
        Assert.assertEquals(user2.getId(), find2.getId());
        Assert.assertEquals(user2.getName(), find2.getName());
        Assert.assertEquals(user2.getPassword(), find2.getPassword());

        userDao.deleteAll();
        Assert.assertTrue(userDao.getCount() == 0);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getNotFountExceptionTest() throws SQLException, ClassNotFoundException {
        Assert.assertTrue(userDao.getCount() == 0);

        User find = userDao.get("이상한Id값");
    }

    @Test
    public void countTest() throws SQLException, ClassNotFoundException {
        userDao.add(this.user1);
        Assert.assertTrue(userDao.getCount() == 1);

        userDao.add(this.user2);
        Assert.assertTrue(userDao.getCount() == 2);

        userDao.add(this.user3);
        Assert.assertTrue(userDao.getCount() == 3);
    }

}