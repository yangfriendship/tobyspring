package springbook.user.service;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static springbook.user.service.UserServiceImpl.MIN_LOGIN_COUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECOMMEND_COUNT_FOR_SILVER;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
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
    private UserDao userDao;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private UserService testUserService;
    @Autowired
    private ApplicationContext context;

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
    public void upgradeLevelsTest() throws Exception {
        // 목 프레임워크를 이용한 UserDao객체 생성
        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);

        // 목 프레임워크를 이용한 MailSender객체 생성
        MailSender mockMail = mock(MailSender.class);

        UserServiceImpl userServiceImpl = new UserServiceImpl();

        userServiceImpl.setMailSender(mockMail);
        userServiceImpl.setUserDao(mockUserDao);

        // 테스트 대상 실행
        userServiceImpl.upgradeLevels();

        // mockUserDao 확인
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        verify(mockUserDao).update(users.get(3));

        // mockMail 확인
        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor
            .forClass(SimpleMailMessage.class);
        verify(mockMail, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        Assert.assertEquals(mailMessages.get(0).getTo()[0], users.get(1).getEmail());
        Assert.assertEquals(mailMessages.get(1).getTo()[0], users.get(3).getEmail());
    }

    private void checkUserAndLevel(User user, String expectedId, Level level) {
        Assert.assertEquals(user.getId(), expectedId);
        Assert.assertEquals(user.getLevel(), level);
    }

    @Test
    @DirtiesContext
    public void upgradeAllOrNothing() throws Exception {

        this.userDao.addAll(this.users);

        try {
            testUserService.upgradeLevels();
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

    @Test
    public void proxyObjectTest(){
        Assert.assertTrue(testUserService instanceof Proxy);
        Assert.assertTrue(userService instanceof Proxy);
    }

    private TestUserService getTestUserService() {
        TestUserService testService = new TestUserService();
        testService.setUserDao(this.userDao);
        DummyMailSender dummyMailSender = new DummyMailSender();
        testService.setMailSender(dummyMailSender);
        this.userDao.addAll(this.users);
        Assert.assertEquals(userDao.getCount(), users.size());
        return testService;
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