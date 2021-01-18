package springbook.learningtest.hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnnotationHelloConfig {

    @Bean
    public AnnotationHello annotationHello() {
        return new AnnotationHello();
    }

}
