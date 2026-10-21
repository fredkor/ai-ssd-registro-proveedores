package mx.com.fredkorlabs.registration;

import mx.com.fredkorlabs.registration.testsupport.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(PostgresTestcontainersConfiguration.class)
@SpringBootTest
class SupplierRegistrationApplicationTests {

	@Test
	void contextLoads() {
	}

}
