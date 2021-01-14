package springbook.learningtest.jdk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

public class ReflectionTest {

    @Test
    public void invokeMethodTest()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String name = "Spring";

        Assert.assertEquals(name.length(), 6);

        Method method = String.class.getMethod("length");
        Assert.assertEquals(method.invoke(name), 6);

    }

    @Test
    public void simpleProxyTest() {
        String name = "youzheng";
        Hello helloTarget = new HelloTarget();
        Assert.assertEquals(helloTarget.sayHello(name), "Hello " + name);
        Assert.assertEquals(helloTarget.sayHi(name), "Hi " + name);
        Assert.assertEquals(helloTarget.sayThankYou(name), "Thank You " + name);

    }

    @Test
    public void helloUppercaseTest() {
        String name = "youzheng";
        HelloUppercase helloTarget = new HelloUppercase(new HelloTarget());
        Assert.assertEquals(helloTarget.sayHello(name), "HELLO " + name.toUpperCase());
        Assert.assertEquals(helloTarget.sayHi(name), "HI " + name.toUpperCase());
        Assert.assertEquals(helloTarget.sayThankYou(name), "THANK YOU " + name.toUpperCase());
    }

    @Test
    public void invocationHandlerTest() {
        Hello helloProxy = (Hello) Proxy.newProxyInstance(
            getClass().getClassLoader()
            , new Class[]{Hello.class}
            , new UppercaseHandler(new HelloTarget())
        );

        String name = "youzheng";
        Assert.assertEquals(helloProxy.sayHello(name), "HELLO " + name.toUpperCase());
        Assert.assertEquals(helloProxy.sayHi(name), "HI " + name.toUpperCase());
        Assert.assertEquals(helloProxy.sayThankYou(name), "THANK YOU " + name.toUpperCase());
    }

    @Test
    public void springProxyFactoryBeanTest() {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new HelloTarget());
        proxyFactoryBean.addAdvice(new UppercaseAdvice());

        Hello hello = (Hello) proxyFactoryBean.getObject();

        String name = "youzheng";
        Assert.assertEquals(hello.sayHello(name), "HELLO " + name.toUpperCase());
        Assert.assertEquals(hello.sayHi(name), "HI " + name.toUpperCase());
        Assert.assertEquals(hello.sayThankYou(name), "THANK YOU " + name.toUpperCase());
    }

    @Test
    public void classNamePointcutTest() {

        NameMatchMethodPointcut classMethodPointCut = new NameMatchMethodPointcut() {
            @Override
            public ClassFilter getClassFilter() {
                return new ClassFilter() {
                    @Override
                    public boolean matches(Class<?> aClass) {
                        return aClass.getSimpleName().startsWith("HelloT");
                    }
                };
            }
        };

        classMethodPointCut.setMappedName("sayH*");

        checkedAdvice(new HelloTarget(), classMethodPointCut, true);
        checkedAdvice(new HelloWorld(), classMethodPointCut, false);
        checkedAdvice(new HelloTYouzheng(), classMethodPointCut, true);

    }

    private void checkedAdvice(Object target, Pointcut pointcut, boolean isAdvice)
         {
        String name = "Youzheng";

        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(target);
        proxyFactoryBean.addAdvisor(new DefaultPointcutAdvisor(pointcut,new UppercaseAdvice()));
        Hello proxyHello = (Hello) proxyFactoryBean.getObject();

        if (isAdvice) {
            Assert.assertEquals(proxyHello.sayHello(name), "HELLO " + name.toUpperCase());
            Assert.assertEquals(proxyHello.sayHi(name), "HI " + name.toUpperCase());
            Assert.assertEquals(proxyHello.sayThankYou(name), "Thank You " + name);
            return;
        }

        Assert.assertEquals(proxyHello.sayHello(name), "Hello " + name);
        Assert.assertEquals(proxyHello.sayHi(name), "Hi " + name);
        Assert.assertEquals(proxyHello.sayThankYou(name), "Thank You " + name);
    }




    private class HelloWorld extends HelloTarget {

    }

    private class HelloTYouzheng extends HelloTarget  {

    }
}
