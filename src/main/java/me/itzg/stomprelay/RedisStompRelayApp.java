package me.itzg.stomprelay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Geoff Bourne
 */
@SpringBootApplication
public class RedisStompRelayApp {
    public static void main(String[] args) {
        SpringApplication.run(RedisStompRelayApp.class, args);
    }
}
