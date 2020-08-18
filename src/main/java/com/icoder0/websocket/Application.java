package com.icoder0.websocket;

import com.icoder0.websocket.core.annotation.EnableWebsocketPlus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author bofa1ex
 * @since 2020/8/2
 */
@EnableWebsocketPlus
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
