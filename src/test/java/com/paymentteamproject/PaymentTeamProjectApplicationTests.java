package com.paymentteamproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.cloud.aws.region.static=ap-northeast-2")
@ActiveProfiles("test")
class PaymentTeamProjectApplicationTests {

    @Test
    void contextLoads() {
    }

}
