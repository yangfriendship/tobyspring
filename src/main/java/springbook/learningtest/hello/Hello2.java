package springbook.learningtest.hello;

import javax.annotation.PostConstruct;

public class Hello2 {

    @PostConstruct
    public void init(){
        System.out.println("Init");
    }

    public void sayHello(){
        System.out.println("Hello");
    }

}
