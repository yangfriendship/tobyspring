package springbook.user;

import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class UserDaoTest {

    private UserDao userDao;
    private ApplicationContext context;

    @Before
    public void setUp() {

        this.context = new GenericXmlApplicationContext(
            "applicationContext.xml");
        this.userDao = this.context.getBean("userDao", UserDao.class);
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

    @Test
    public void addTestWithContext() throws SQLException, ClassNotFoundException {

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

}