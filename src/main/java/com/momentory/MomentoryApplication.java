package com.momentory;

import com.momentory.common.time.TimeZonePolicy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class MomentoryApplication {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone(TimeZonePolicy.DEFAULT_ZONE_ID));
    }

    public static void main(String[] args) {
        SpringApplication.run(MomentoryApplication.class, args);
    }
}
