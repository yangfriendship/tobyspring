package springbook.user;

public class DaoFactory {

    public UserDao userDao() {
        YConnectionMaker connectionMaker = new YConnectionMaker();
        UserDao userDao = new UserDao(connectionMaker);
        return userDao;
    }
}
