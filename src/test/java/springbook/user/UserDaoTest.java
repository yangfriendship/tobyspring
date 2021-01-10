package springbook.user;

import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;


public class UserDaoTest {

    private UserDao userDao;
    private ApplicationContext context;

    private User user;

    @Before
    public void setUp() {
        this.context = new GenericXmlApplicationContext(
            "applicationContext.xml");
        this.userDao = this.context.getBean("userDao", UserDao.class);

        this.user = new User("1", "youzheng", "ps");
    }

    @Test
    public void addTest() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();
        User user = new User("1", "name", "ps");
        userDao.add(user);

        User find = userDao.get(user.getId());

        Assert.assertEquals(user.getId(), find.getId());
        Assert.assertEquals(user.getName(), find.getName());
        Assert.assertEquals(user.getPassword(), find.getPassword());

    }

    @Test
    public void addTestWithContext() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();
        User user = new User("1", "name", "ps");
        userDao.add(user);

        User find = userDao.get(user.getId());

        Assert.assertEquals(user.getId(), find.getId());
        Assert.assertEquals(user.getName(), find.getName());
        Assert.assertEquals(user.getPassword(), find.getPassword());
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
        System.out.println("userDao1 = " + userDao1);
        System.out.println("userDao2 = " + userDao2);
        Assert.assertSame(userDao1, userDao2);
    }

    @Test
    public void addAndGetTest() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();
        Assert.assertTrue(userDao.getCount() == 0);

        userDao.add(this.user);
        Assert.assertTrue(userDao.getCount() == 1);

        User user2 = new User("2", "name2", "ps2");
        userDao.add(user2);
        Assert.assertTrue(userDao.getCount() == 2);

        User find = userDao.get(this.user.getId());
        Assert.assertEquals(this.user.getId(), find.getId());
        Assert.assertEquals(this.user.getName(), find.getName());
        Assert.assertEquals(this.user.getPassword(), find.getPassword());

        User find2 = userDao.get(user2.getId());
        Assert.assertEquals(user2.getId(), find2.getId());
        Assert.assertEquals(user2.getName(), find2.getName());
        Assert.assertEquals(user2.getPassword(), find2.getPassword());

        userDao.deleteAll();
        Assert.assertTrue(userDao.getCount() == 0);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getNotFountExceptionTest() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();
        Assert.assertTrue(userDao.getCount() == 0);

        User find = userDao.get("이상한Id값");
    }

    @Test
    public void countTest() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();
        userDao.add(new User("1", "name1", "ps1"));
        Assert.assertTrue(userDao.getCount() == 1);

        userDao.add(new User("2", "name2", "ps2"));
        Assert.assertTrue(userDao.getCount() == 2);

        userDao.add(new User("3", "name3", "ps3"));
        Assert.assertTrue(userDao.getCount() == 3);
    }

}