package com.commerce.pagopa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PagopaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagopaApplication.class, args);
    }

}
