package springbook.user.service;

import static springbook.user.service.UserService.MIN_LOGIN_COUNT_FOR_SILVER;
import static springbook.user.service.UserService.MIN_RECOMMEND_COUNT_FOR_SILVER;

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
            new User("1", "user1", "ps1", Level.BASIC, MIN_LOGIN_COUNT_FOR_SILVER - 1, 0),
            new User("2", "user2", "ps2", Level.BASIC, MIN_LOGIN_COUNT_FOR_SILVER, 0),
            new User("3", "user3", "ps3", Level.SILVER, 60, MIN_RECOMMEND_COUNT_FOR_SILVER - 1),
            new User("4", "user4", "ps4", Level.SILVER, 60, MIN_RECOMMEND_COUNT_FOR_SILVER),
            new User("5", "user5", "ps5", Level.GOLD, MIN_LOGIN_COUNT_FOR_SILVER * 2,
                MIN_LOGIN_COUNT_FOR_SILVER * 2));
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

        Assert.assertEquals(userWithOutLevelFind.getLevel(), Level.BASIC);
        Assert.assertEquals(userWithLevel.getLevel(), userWithLevelFind.getLevel());
    }

    @Test
    public void upgradeLevelTest() {
        for (Level level : Level.values()) {
            User user = users.get(0);
            if (level.nextLevel() == null) {
                continue;
            }
            user.setLevel(level);
            user.upgradeLevel();
            Assert.assertEquals(user.getLevel(), level.nextLevel());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void upgradeLevelFailureTest() {
        for (Level level : Level.values()) {
            User user = users.get(0);
            if (level.nextLevel() != null) {
                continue;
            }
            user.setLevel(level);
            user.upgradeLevel();
            Assert.assertEquals(user.getLevel(), level.nextLevel());
        }
    }

    @Test
    public void upgradeLevelsTest() {
        userDao.addAll(this.users);

        userService.upgradeLevels();
        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);
    }

    private void checkLevelUpgraded(User user, boolean expected) {
        User upgradedUser = userDao.get(user.getId());
        if (expected) {
            Assert.assertEquals(upgradedUser.getLevel(), user.getLevel().nextLevel());
            return;
        }
        Assert.assertEquals(upgradedUser.getLevel(), user.getLevel());
    }

    private void checkLevel(User user, Level expected) {
        User updatedUser = userDao.get(user.getId());
        Assert.assertSame(expected, updatedUser.getLevel());
    }


}