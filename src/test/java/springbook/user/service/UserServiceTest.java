package springbook.user.service;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springbook.user.Level;
import springbook.user.User;
import springbook.user.dao.UserDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDao userDao;
    private List<User> users;


    @Before
    public void setUp() {
        this.users = Arrays.asList(
            new User("1", "user1", "ps1", Level.BASIC, 49, 0),
            new User("2", "user2", "ps2", Level.BASIC, 50, 0),
            new User("3", "user3", "ps3", Level.SILVER, 60, 29),
            new User("4", "user4", "ps4", Level.SILVER, 60, 30),
            new User("5", "user5", "ps5", Level.GOLD, 100, 100));
    }

    @Before
    public void reset() {
        userDao.deleteAll();
    }

    @Test
    public void autowiredTest() {
        Assert.assertNotNull(userService);
    }

    @Test
    public void updateLevelsTest() {
        userDao.addAll(this.users);
        Assert.assertTrue(userDao.getCount() == 5);

        userService.upgradeLevels();

        checkLevel(users.get(0), Level.BASIC);
        checkLevel(users.get(1), Level.SILVER);
        checkLevel(users.get(2), Level.SILVER);
        checkLevel(users.get(3), Level.GOLD);
        checkLevel(users.get(4), Level.GOLD);
    }

    @Test
    public void addTest() {
        User userWithLevel = users.get(1);
        User userWithOutLevel = users.get(0);
        userWithOutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithOutLevel);

        User userWithLevelFind = userDao.get(userWithLevel.getId());
        User userWithOutLevelFind = userDao.get(userWithOutLevel.getId());

        Assert.assertEquals(userWithOutLevel.getLevel(),Level.BASIC);
        Assert.assertEquals(userWithLevel.getLevel(),userWithLevelFind.getLevel());
    }

    private void checkLevel(User user, Level expected) {
        User updatedUser = userDao.get(user.getId());
        Assert.assertSame(expected, updatedUser.getLevel());
    }


}