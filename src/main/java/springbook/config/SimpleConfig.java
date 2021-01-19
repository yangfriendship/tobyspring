package springbook.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springbook.learningtest.hello.Hello2;

@Configuration
public class SimpleConfig {

    @Autowired
    private Hello2 hello2;

    @Bean
    public Hello2 hello2() {
        return new Hello2();
    }


}
