package springbook.user;

import java.sql.SQLException;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserDaoTest {


    @Autowired
    private UserDao userDao;
    private ApplicationContext context;


    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp() {
        this.context = new GenericXmlApplicationContext(
            "applicationContext.xml");
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

    @Test
    public void getAllTest() throws SQLException, ClassNotFoundException {
        // 테이블이 비어있다면 비어있는 리스트를 반환한다.
        userDao.deleteAll();
        List<User> users0 = userDao.getAll();
        Assert.assertEquals(users0.size(),0);

        userDao.add(this.user1);
        List<User> users = userDao.getAll();
        Assert.assertEquals(users.size(), 1);

        userDao.add(this.user3);
        List<User> users2 = userDao.getAll();
        Assert.assertEquals(users2.size(), 2);

        userDao.add(this.user2);
        List<User> users3 = userDao.getAll();
        Assert.assertEquals(users3.size(), 3);

        checkSameUser(user1, users3.get(0));
        checkSameUser(user2, users3.get(1));
        checkSameUser(user3, users3.get(2));
    }

    private void checkSameUser(User user1, User user2) {
        Assert.assertEquals(user1.getId(), user2.getId());
        Assert.assertEquals(user1.getName(), user2.getName());
        Assert.assertEquals(user1.getPassword(), user2.getPassword());
    }
}