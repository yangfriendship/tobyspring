package springbook.learningtest.jdk;

import static org.junit.Assert.fail;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionTest {

    @Test
    public void invokeMethodTest()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String name = "Spring";

        Assert.assertEquals(name.length(),6);

        Method method = String.class.getMethod("length");
        Assert.assertEquals(method.invoke(name) ,6);

    }

    @Test
    public void simpleProxyTest(){
        String name = "youzheng";
        Hello helloTarget = new HelloTarget();
        Assert.assertEquals(helloTarget.sayHello(name),"Hello "+name);
        Assert.assertEquals(helloTarget.sayHi(name),"Hi "+name);
        Assert.assertEquals(helloTarget.sayThankYou(name),"Thank You "+name);

    }

    @Test
    public void helloUppercaseTest(){
        String name = "youzheng";
        HelloUppercase helloTarget = new HelloUppercase(new HelloTarget());
        Assert.assertEquals(helloTarget.sayHello(name),"HELLO "+name.toUpperCase());
        Assert.assertEquals(helloTarget.sayHi(name),"HI "+name.toUpperCase());
        Assert.assertEquals(helloTarget.sayThankYou(name),"THANK YOU "+name.toUpperCase());
    }

    @Test
    public void invocationHandlerTest(){
        Hello helloProxy = (Hello) Proxy.newProxyInstance(
            getClass().getClassLoader()
            , new Class[]{Hello.class}
            , new UppercaseHandler(new HelloTarget())
        );

        String name = "youzheng";
        Assert.assertEquals(helloProxy.sayHello(name),"HELLO "+name.toUpperCase());
        Assert.assertEquals(helloProxy.sayHi(name),"HI "+name.toUpperCase());
        Assert.assertEquals(helloProxy.sayThankYou(name),"THANK YOU "+name.toUpperCase());
    }


}
