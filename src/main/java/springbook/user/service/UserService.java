package springbook.user.service;

import java.util.List;
import springbook.user.User;

public interface UserService {

    void add(User user);

    void add(List<User> user);

    User get(String id);

    List<User> getAll();

    void deleteAll();

    void upgradeLevels();

    void update(User user);
}
