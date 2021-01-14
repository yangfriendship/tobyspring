package springbook.learningtest.pointcut;

import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

public class PointcutTest {

    @Test
    public void pointcutTest() throws NoSuchMethodException {

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(public int + "
            + "springbook.learningtest.pointcut.Target.minus(int,int) "
            + "throws java.lang.RuntimeException)");

        Assert.assertTrue(pointcut.getClassFilter().matches(Target.class));
        Assert.assertTrue(pointcut.getMethodMatcher()
            .matches(Target.class.getMethod("minus", int.class, int.class), null));

        Assert.assertFalse(pointcut.getMethodMatcher()
            .matches(Target.class.getMethod("plus", int.class, int.class), null));

        Assert.assertTrue(pointcut.getClassFilter().matches(Bean.class));
    }


}
