package springbook.user.service;

import java.util.List;
import org.springframework.stereotype.Service;
import springbook.user.User;

@Service("testUserService")
public class TestUserService extends UserServiceImpl {

    private static final String DEFAULT_TARGET_ID = "4";
    private String EXCEPTION_TARGET_ID = DEFAULT_TARGET_ID;

    public TestUserService() {
    }

    
    /*
    * readOnly 예외를 위한 메서드 재구현
    * */
    @Override
    public List<User> getAll() {

        for(User user : super.getAll()){
            super.update(user);
        }
        return null;
    }

    public TestUserService(String EXCEPTION_TARGET_ID) {
        this.EXCEPTION_TARGET_ID = EXCEPTION_TARGET_ID;
    }

    @Override
    protected void upgradeLevel(User user) {
        if (user.getId().equals(EXCEPTION_TARGET_ID)) {
            throw new TestUserServiceException();
        }
        super.upgradeLevel(user);
    }

    static class TestUserServiceException extends RuntimeException {

    }
}
