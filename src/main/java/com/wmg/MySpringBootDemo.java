package com.wmg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MySpringBootDemo {
    public static void main(String[] args) {
        //入口
        SpringApplication.run(MySpringBootDemo.class,args);
        System.out.println("http://127.0.0.1:8080");
    }
}
