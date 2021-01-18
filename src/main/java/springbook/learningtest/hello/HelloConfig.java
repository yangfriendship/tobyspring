package springbook.learningtest.hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloConfig {

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
