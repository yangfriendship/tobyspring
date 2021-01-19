package springbook.learningtest.hello;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ServiceRequest {

    private String name;

    public void sayHi() {
        System.out.println("Hi ");
        System.out.println(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void init() {
        this.name = "youzheng";
    }

}
