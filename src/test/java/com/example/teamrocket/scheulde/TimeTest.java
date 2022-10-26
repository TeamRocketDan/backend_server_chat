package com.example.teamrocket.scheulde;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TimeZone;

public class TimeTest {

    @Test
    @DisplayName("Time testing")
    public void timeTesting() throws Exception{
        TimeZone aDefault = TimeZone.getDefault();
        Assertions.assertThat(aDefault.getID()).isEqualTo("Asia/Seoul");
    }
}
