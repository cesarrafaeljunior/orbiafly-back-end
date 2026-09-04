package com.orbiafly.api;

import com.orbiafly.api.support.TestCurrentUserConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestCurrentUserConfig.class)
@ActiveProfiles("test")
class OrbiaflyApplicationTests {

  @Test
  void contextLoads() {
  }
}
