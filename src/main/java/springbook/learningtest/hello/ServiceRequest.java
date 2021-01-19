package springbook.learningtest.hello;

import javax.inject.Provider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ServiceRequest {

    public void sayHi(){
        System.out.println("Hi ");
    }

}
