package springbook.learningtest.hello;

import java.util.Properties;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class HelloConfig {

    @Resource
    private Properties systemProperties;

    @Bean
    public Hello hello(){
        Hello hello = new Hello();
        hello.setName("hello");
        hello.setPrinter(printer());
        return hello;
    }

    @Bean
    public Hello hello2(){
        Hello hello = new Hello();
        hello.setName("hello2");
        hello.setPrinter(printer());
        return hello;
    }

    @Bean
    public Printer printer() {
        return new StringPrinter();
    }

}
