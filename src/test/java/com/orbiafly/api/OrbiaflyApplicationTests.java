package com.orbiafly.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(AuthenticatedUserTestConfiguration.class)
@ActiveProfiles("test")
class OrbiaflyApplicationTests {

  @Test
  void contextLoads() {
  }
}
