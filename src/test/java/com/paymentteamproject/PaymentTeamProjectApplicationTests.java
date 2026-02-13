package com.paymentteamproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.cloud.aws.region.static=ap-northeast-2")
class PaymentTeamProjectApplicationTests {

    @Test
    void contextLoads() {
    }

}
