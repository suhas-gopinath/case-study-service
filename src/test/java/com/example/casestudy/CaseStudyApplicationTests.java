package com.example.casestudy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class CaseStudyApplicationTests {

	@Test
	void contextLoads() {
        assert true;
	}

}
