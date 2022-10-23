package com.example.teamrocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.teamrocket.chatRoom.repository.mongo")
public class TeamRocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamRocketApplication.class, args);
    }

}
