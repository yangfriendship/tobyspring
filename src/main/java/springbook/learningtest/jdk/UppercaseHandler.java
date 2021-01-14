package springbook.learningtest.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UppercaseHandler implements InvocationHandler {

    private Object target;

    public UppercaseHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result =  method.invoke(target,args);    // 타켓에게 위임

        if(result instanceof String){
            return ((String) result).toUpperCase();    // 부가기능 실행
        }
        return result;
    }
}
