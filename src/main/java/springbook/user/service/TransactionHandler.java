package springbook.user.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    // 타겟 오브젝트
    private Object target;
    // 부가기능을 위한 오브젝트
    private PlatformTransactionManager transactionManager;
    // 부가기능 적용 대상 메서드의 패턴
    private String pattern;

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setTransactionManager(
        PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (method.getName().startsWith(pattern)) {
            return invocationTransaction(method, args);
        }
        return method.invoke(target, args);
    }

    private Object invocationTransaction(Method method, Object[] args)
        throws InvocationTargetException, IllegalAccessException {
        TransactionStatus status = transactionManager
            .getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            this.transactionManager.commit(status);
            return result;
        } catch (InvocationTargetException e) {
            this.transactionManager.rollback(status);
            throw e;
        } catch (IllegalAccessException e) {
            this.transactionManager.rollback(status);
            throw e;
        }
    }
}
