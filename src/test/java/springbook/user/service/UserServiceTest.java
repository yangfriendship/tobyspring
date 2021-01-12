package springbook.user.service;

import static org.junit.Assert.fail;
import static springbook.user.service.UserServiceImpl.MIN_LOGIN_COUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECOMMEND_COUNT_FOR_SILVER;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import springbook.user.Level;
import springbook.user.User;
import springbook.user.dao.UserDao;
import springbook.user.service.TestUserService.TestUserServiceException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserServiceImpl userServiceImpl;
    @Autowired
    private UserDao userDao;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private List<User> users;


    @Before
    public void setUp() {
        this.users = Arrays.asList(
            new User("1", "user1", "ps1", Level.BASIC, MIN_LOGIN_COUNT_FOR_SILVER - 1, 0,
                "youzheng1@gmail.com"),
            new User("2", "user2", "ps2", Level.BASIC, MIN_LOGIN_COUNT_FOR_SILVER, 0,
                "youzheng2@gmail.com"),
            new User("3", "user3", "ps3", Level.SILVER, 60, MIN_RECOMMEND_COUNT_FOR_SILVER - 1,
                "youzheng3@gmail.com"),
            new User("4", "user4", "ps4", Level.SILVER, 60, MIN_RECOMMEND_COUNT_FOR_SILVER,
                "youzheng4@gmail.com"),
            new User("5", "user5", "ps5", Level.GOLD, MIN_LOGIN_COUNT_FOR_SILVER * 2,
                MIN_LOGIN_COUNT_FOR_SILVER * 2, "youzheng5@gmail.com"));
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
    public void updateLevelsTest() throws Exception {
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
    @DirtiesContext
    public void upgradeLevelsTest() throws Exception {
        userDao.addAll(this.users);
        MockMailSender mailSender = new MockMailSender();

        userServiceImpl.setMailSender(mailSender);
        userServiceImpl.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);

        List<String> requests = mailSender.getRequests();

        Assert.assertEquals(requests.get(0), this.users.get(1).getEmail());
        Assert.assertEquals(requests.get(1), this.users.get(3).getEmail());
    }

    @Test
    public void upgradeAllOrNothing() {
        TestUserService testService = new TestUserService();
        testService.setUserDao(this.userDao);
        DummyMailSender dummyMailSender = new DummyMailSender();
        testService.setMailSender(dummyMailSender);
        this.userDao.addAll(this.users);
        Assert.assertEquals(userDao.getCount(), users.size());

        UserServiceTx userServiceTx = new UserServiceTx();
        userServiceTx.setUserService(testService);
        userServiceTx.setTransactionManager(transactionManager);

        try {
            userServiceTx.upgradeLevels();
            fail("TestUserServiceException expected");
        } catch (TestUserServiceException e) {
        } catch (Exception e) {

        }
        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), false);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), false);
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