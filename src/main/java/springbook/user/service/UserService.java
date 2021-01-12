package springbook.user.service;

import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import springbook.user.Level;
import springbook.user.User;
import springbook.user.dao.UserDao;

public class UserService {

    public static final int MIN_LOGIN_COUNT_FOR_SILVER = 50;
    public static final int MIN_RECOMMEND_COUNT_FOR_SILVER = 30;

    private UserDao userDao;
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void add(User user) {
        if (user.getLevel() == null) {
            user.setLevel(Level.BASIC);
        }
        userDao.add(user);
    }

    public void upgradeLevels() throws Exception {
        //트랜잭션 관리자를 이용해 동기화 작업을 초기화
        TransactionSynchronizationManager.initSynchronization();
        //DB컨넥션을 생성
        Connection connection = DataSourceUtils.getConnection(this.dataSource);
        //트랜잭션을 실행
        connection.setAutoCommit(false);

        try {
            List<User> users = userDao.getAll();
            for (User user : users) {
                if (canUpgradeUser(user)) {
                    upgradeLevel(user);
                }
            }
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            // DB컨넥션 반환
            DataSourceUtils.releaseConnection(connection, this.dataSource);
            // 동기화 해제
            TransactionSynchronizationManager.unbindResource(this.dataSource);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    protected void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
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
