package com.icoder0.websocket.example;

import com.icoder0.websocket.spring.annotation.EnableWebsocketPlus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author bofa1ex
 * @since 2020/8/19
 */
@EnableWebsocketPlus
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}