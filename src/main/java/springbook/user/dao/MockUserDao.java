package springbook.user.dao;

import java.util.ArrayList;
import java.util.List;
import springbook.user.User;

public class MockUserDao implements UserDao {

    private List<User> users;
    private List<User> updated = new ArrayList<User>();

    public MockUserDao(List<User> users) {
        this.users = users;
    }

    @Override
    public List<User> getAll() {
        return this.users;
    }

    @Override
    public void update(User user) {
        updated.add(user);
    }

    public List<User> getUpdated() {
        return this.updated;
    }


    @Override
    public void add(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAll(User... users) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAll(List<User> users) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User get(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCount() {
        throw new UnsupportedOperationException();
    }

}
