package com.example.bookex;

import com.example.bookex.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class BookExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookExchangeApplication.class, args);
    }

}
