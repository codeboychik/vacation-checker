package com.example.vacationchecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VacationCheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VacationCheckerApplication.class, args);
    }
}
