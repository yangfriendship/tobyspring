package springbook.learningtest.jdk;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class UppercaseAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String proceed = (String) methodInvocation.proceed();
        return proceed.toUpperCase();
    }
}