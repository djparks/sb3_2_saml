package com.example.sb3_2_saml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the sb3_2_saml application.
 *
 * This Spring Boot 3.2 application uses Java 17 and enables SAML2 login via Spring Security.
 */
@SpringBootApplication
public class Sb32SamlApplication {

    public static void main(String[] args) {
        SpringApplication.run(Sb32SamlApplication.class, args);
    }
}
