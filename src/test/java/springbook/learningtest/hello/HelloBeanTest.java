package springbook.learningtest.hello;

import com.ibatis.common.resources.Resources;
import java.io.IOException;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Provider;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.support.XmlWebApplicationContext;
import springbook.config.SimpleConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/vol2/helloAppContext.xml")
public class HelloBeanTest {

    private StaticApplicationContext context;
    private GenericApplicationContext genericContext;

    @Autowired
    private ServiceRequestFactory serviceRequestFactory;
    @Inject
    private Provider<ServiceRequest> serviceRequestProvider;


    @Before
    public void setUp() {
        this.context = new StaticApplicationContext();
        this.genericContext = new GenericApplicationContext();
    }

    @Test
    public void systemPropertiesTest() {
        ApplicationContext context = new GenericXmlApplicationContext(
            "/vol2/helloAppContext.xml");
        Environment environment = context.getBean(Environment.class);

    }

    @Test
    public void xmlPropertiesBeanTest() {
        ApplicationContext context = new GenericXmlApplicationContext(
            "/vol2/helloAppContext.xml");
        Properties database = context.getBean("database", Properties.class);
        for (String name : database.stringPropertyNames()) {
            System.out.printf("%s : %s \n   ", name, database.get(name));
        }
    }

    @Test
    public void propertiesLoadFIleTest() throws IOException {
        Properties prop = new Properties();
        prop.load(Resources.getResourceAsStream("database.properties"));
        for (String name : prop.stringPropertyNames()) {
            System.out.printf("%s : %s \n   ", name, prop.get(name));
        }
    }

    @Test
    public void componentBeanTest() {
        ApplicationContext context = new GenericXmlApplicationContext(
            "/vol2/helloAppContext.xml");

        SimpleConfig config = context.getBean(SimpleConfig.class);
        config.hello2().sayHello();
    }


    @Test
    public void initMethodTest() {
        GenericXmlApplicationContext context = new GenericXmlApplicationContext(
            "/vol2/helloAppContext.xml");

        ServiceRequest request = context.getBean("serviceRequest", ServiceRequest.class);
        Assert.assertNotNull(request);
        Assert.assertEquals("youzheng", request.getName());
    }

    @Test
    public void providerTest() {
        ServiceRequest serviceRequest = serviceRequestProvider.get();
        ServiceRequest serviceRequest2 = serviceRequestProvider.get();

        Assert.assertNotNull(serviceRequest);
        Assert.assertNotNull(serviceRequest2);
        Assert.assertNotSame(serviceRequest, serviceRequest2);
    }

    @Test
    public void serviceRequestFactoryTest() {
        ServiceRequest serviceRequest = serviceRequestFactory.getServiceRequest();
        ServiceRequest serviceRequest2 = serviceRequestFactory.getServiceRequest();

        Assert.assertNotNull(serviceRequest);
        Assert.assertNotNull(serviceRequest2);
        Assert.assertNotSame(serviceRequest, serviceRequest2);
    }

    @Test
    public void environmentTest() {
        ApplicationContext context = new AnnotationConfigApplicationContext(
            HelloConfig.class);
        Properties properties = context.getBean("systemProperties", Properties.class);
        for (String prop : properties.stringPropertyNames()) {
            System.out.println(prop.toString() + " : " + properties.get(prop));
        }
        Assert.assertNotNull(properties);
    }

    @Test
    public void helloConfigSingleTonTest() {
        ApplicationContext context = new AnnotationConfigApplicationContext(
            HelloConfig.class);
        Hello hello = context.getBean("hello", Hello.class);
        Hello hello2 = context.getBean("hello2", Hello.class);

        Assert.assertNotSame(hello, hello2);
        Assert.assertSame(hello.getPrinter(), hello2.getPrinter());
    }

    @Test
    public void javaCodeConfigurationWithXmlContextTest() {

        GenericXmlApplicationContext context = new GenericXmlApplicationContext(
            "/vol2/helloAppContext.xml");

        AnnotationHello hello = context.getBean("annotationHello", AnnotationHello.class);

        Assert.assertNotNull(hello);
    }

    @Test
    public void javaCodeConfigurationTest() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
            AnnotationHelloConfig.class);
        Hello hello = context.getBean("annotationHello", Hello.class);
        Assert.assertNotNull(hello);
    }

    @Test
    public void annotationConfigApplicationContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
            "springbook/learningtest");

        AnnotationHello hello = context.getBean("annotationHello", AnnotationHello.class);
        Assert.assertNotNull(hello);
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
    public void childAndParentContextTest() {

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
        Assert.assertEquals("Hello child", childHello.sayHello());
        Assert.assertEquals("Hello parent", parentHello.sayHello());
    }

    @Test
    public void childAndParentContextTest2() {

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