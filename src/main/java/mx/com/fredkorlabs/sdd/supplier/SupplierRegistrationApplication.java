package mx.com.fredkorlabs.sdd.supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "mx.com.fredkorlabs.sdd.supplier",
    "mx.com.fredkorlabs.registration"
})
public class SupplierRegistrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupplierRegistrationApplication.class, args);
	}

}
