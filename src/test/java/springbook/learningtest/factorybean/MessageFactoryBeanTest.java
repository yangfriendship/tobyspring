package springbook.learningtest.factorybean;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springbook.config.AppContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppContext.class)
@ActiveProfiles("test")
public class MessageFactoryBeanTest {

    @Test
    @Ignore
    public void factoryBeanTest() {
        ApplicationContext context = new GenericXmlApplicationContext(
            "/applicationContext.xml");
        Message message = context.getBean("message", Message.class);
        Assert.assertEquals("Factory Bean", message.getText());

        // &를 붙이면 FactoryBean 구현체를 가져온다.
        Object factoryBean = context.getBean("&message");
        Assert.assertTrue(factoryBean instanceof MessageFactoryBean);

    }
}