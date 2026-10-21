package mx.com.fredkorlabs.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "mx.com.fredkorlabs.registration"
})
public class SupplierRegistrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupplierRegistrationApplication.class, args);
	}

}
