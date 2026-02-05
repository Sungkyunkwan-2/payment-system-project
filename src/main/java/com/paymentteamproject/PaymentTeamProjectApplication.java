package com.paymentteamproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PaymentTeamProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentTeamProjectApplication.class, args);
    }

}
