package com.example.teamrocket.scheulde;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.TimeZone;

@SpringBootTest
public class TimeTest {
    int count = 0;
    @Scheduled(fixedDelay = 5)
    void setSchedule(){
            count++;
    }

    @Test
    @DisplayName("Time testing")
    public void timeTesting() throws Exception{
        TimeZone aDefault = TimeZone.getDefault();
        Assertions.assertThat(aDefault.getID()).isEqualTo("Asia/Seoul");
    }

    @Test
    @DisplayName("Schedule testing")
    void scheduleTest() throws Exception{
        Assertions.assertThat(count).isGreaterThan(0);
    }
}
