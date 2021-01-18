package springbook.learningtest.hello;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class HelloBeanTest {

    private StaticApplicationContext context;
    private GenericApplicationContext genericContext;

    @Before
    public void setUp() {
        this.context = new StaticApplicationContext();
        this.genericContext = new GenericApplicationContext();
    }

    @Test
    public void staticApplicationContextTest() {

        context.registerSingleton("hello1", Hello.class);

        Hello hello = context.getBean("hello1", Hello.class);

        Assert.assertNotNull(hello);
    }

    @Test
    public void beanDefinitionRegisterTest() {
        String name = "youzheng";
        String beanName = "hello";

        RootBeanDefinition definition = new RootBeanDefinition(Hello.class);
        definition.getPropertyValues()
            .addPropertyValue("name", name);

        context.registerBeanDefinition(beanName, definition);

        Hello hello = context.getBean(beanName, Hello.class);

        Assert.assertNotNull(hello);
        Assert.assertEquals(name, hello.getName());
    }

    @Test
    public void hasDuplicateTypeBeansTest() {
        String name = "youzheng";
        String beanName = "hello";

        RootBeanDefinition definition = new RootBeanDefinition(Hello.class);
        definition.getPropertyValues()
            .addPropertyValue("name", name);

        RootBeanDefinition definition2 = new RootBeanDefinition(Hello.class);
        definition.getPropertyValues()
            .addPropertyValue("name", name);

        context.registerBeanDefinition(beanName, definition);
        context.registerBeanDefinition("hello2", definition2);

        Hello hello = context.getBean(beanName, Hello.class);

        Assert.assertEquals(2, context.getBeanFactory().getBeanDefinitionCount());
    }

    @Test
    public void registerBeanWithDependency() {
        context.registerBeanDefinition("printer", new RootBeanDefinition(StringPrinter.class));

        RootBeanDefinition beanDefinition = new RootBeanDefinition(Hello.class);
        beanDefinition.getPropertyValues()
            .addPropertyValue("name", "youzheng");
        beanDefinition.getPropertyValues()
            .addPropertyValue("printer", new RuntimeBeanReference("printer"));

        context.registerBeanDefinition("hello", beanDefinition);

        Hello hello = context.getBean("hello", Hello.class);
        Assert.assertEquals("Hello youzheng", hello.sayHello());
    }

    @Test
    public void genericApplicationContextTest() {

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.genericContext);
        reader.loadBeanDefinitions("/vol2/helloAppContext.xml");
        this.genericContext.refresh();

        Hello hello = this.genericContext.getBean("hello", Hello.class);
        Assert.assertNotNull(hello);
        Assert.assertEquals("Hello youzheng", hello.sayHello());
    }

    @Test
    public void genericXmlApplicationContext() {
        ApplicationContext context = new GenericXmlApplicationContext(
            "/vol2/helloAppContext.xml");
        Hello hello = context.getBean("hello", Hello.class);
        Assert.assertNotNull(hello);
        Assert.assertEquals("Hello youzheng", hello.sayHello());

        new XmlWebApplicationContext();
    }

    @Test
    public void childAndParentContextTest(){

        GenericXmlApplicationContext parentContext = new GenericXmlApplicationContext(
            "/vol2/parentContext.xml");

        GenericApplicationContext childContext = new GenericApplicationContext(
            parentContext);

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(childContext);
        int result = reader.loadBeanDefinitions("vol2/childContext.xml");
        childContext.refresh();

        Hello parentHello = parentContext.getBean("hello", Hello.class);
        Hello childHello = childContext.getBean("hello", Hello.class);
        Assert.assertNotNull(childHello);
        Assert.assertEquals("Hello child",childHello.sayHello());
        Assert.assertEquals("Hello parent",parentHello.sayHello());
    }

    @Test
    public void childAndParentContextTest2(){

        GenericXmlApplicationContext parentContext = new GenericXmlApplicationContext(
            "/vol2/parentContext.xml");

        GenericApplicationContext childContext = new GenericApplicationContext(
            parentContext);

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(childContext);
        int result = reader.loadBeanDefinitions("vol2/childContext.xml");
        childContext.refresh();

        Printer printer = childContext.getBean("printer", Printer.class);
        Assert.assertNotNull(printer);
        Assert.assertTrue(printer instanceof StringPrinter);
    }

}