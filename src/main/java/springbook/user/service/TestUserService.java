package springbook.user.service;

import springbook.user.User;

public class TestUserService extends UserServiceImpl {

    private static final String DEFAULT_TARGET_ID = "4";
    private String EXCEPTION_TARGET_ID = DEFAULT_TARGET_ID;

    public TestUserService() {
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
