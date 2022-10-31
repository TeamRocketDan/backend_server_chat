package com.example.teamrocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.TimeZone;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.teamrocket.chatRoom.repository.mongo")
@EnableScheduling
public class TeamRocketApplication {

    @PostConstruct
    public void started(){
        TimeZone.setDefault(TimeZone.getTimeZone("KST"));
    }

    public static void main(String[] args) {
        SpringApplication.run(TeamRocketApplication.class, args);
    }

}
