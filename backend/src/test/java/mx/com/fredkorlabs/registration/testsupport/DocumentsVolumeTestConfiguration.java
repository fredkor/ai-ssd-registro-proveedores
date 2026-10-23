package mx.com.fredkorlabs.registration.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** ADR-0010: documents volume in a temporary directory for the test run, never the real /repository. */
@TestConfiguration(proxyBeanMethods = false)
public class DocumentsVolumeTestConfiguration {

    public static final Path ROOT = createRoot();

    private static Path createRoot() {
        try {
            Path root = Files.createTempDirectory("registration-documents-");
            root.toFile().deleteOnExit();
            return root;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Bean
    DynamicPropertyRegistrar documentsRootLocation() {
        return registry -> registry.add("registration.documents.root-location", ROOT::toString);
    }
}
