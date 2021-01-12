package springbook.user.service;

import java.util.List;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import springbook.user.Level;
import springbook.user.User;
import springbook.user.dao.UserDao;

public class UserServiceImpl implements UserService {

    public static final int MIN_LOGIN_COUNT_FOR_SILVER = 50;
    public static final int MIN_RECOMMEND_COUNT_FOR_SILVER = 30;

    private UserDao userDao;
    private MailSender mailSender;

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void add(User user) {
        if (user.getLevel() == null) {
            user.setLevel(Level.BASIC);
        }
        userDao.add(user);
    }

    @Override
    public void upgradeLevels() {
        upgradeLevelsInternal();
    }

    private void upgradeLevelsInternal() {
        List<User> users = userDao.getAll();
        for (User user : users) {
            if (canUpgradeUser(user)) {
                upgradeLevel(user);
            }
        }
    }

    protected void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
        sendUpgradeEmail(user);
    }

    private void sendUpgradeEmail(User user) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom("me@naver.com");
        message.setSubject("업그레이드 안내");
        message.setText("사용자님의 등급이 " + user.getLevel().name() + "으로 업그레이드되었습니다.");

        mailSender.send(message);
    }

    private boolean canUpgradeUser(User user) {
        Level currentLevel = user.getLevel();

        switch (currentLevel) {
            case BASIC: {
                return user.getLogin() >= MIN_LOGIN_COUNT_FOR_SILVER;
            }
            case SILVER: {
                return user.getRecommend() >= MIN_RECOMMEND_COUNT_FOR_SILVER;
            }
            case GOLD: {
                return false;
            }
            default: {
                throw new IllegalArgumentException("Unknown Level:" + currentLevel);
            }
        }

    }

}
