package mx.com.fredkorlabs.registration.adapter.in.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** The multipart schemas do not allow additional parts: an undefined or repeated part is rejected. */
final class MultipartParts {

    private MultipartParts() {
    }

    static void requireOnly(Set<String> definedFileParts) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();
        Map<String, Integer> fileParts = new HashMap<>();
        MultipartRequest resolved = WebUtils.getNativeRequest(request, MultipartRequest.class);
        if (resolved != null) {
            resolved.getMultiFileMap().forEach((name, files) -> fileParts.put(name, files.size()));
        } else {
            // The stored request is the container's one; read its parts with the Servlet API.
            for (Part part : parts(request)) {
                if (part.getSubmittedFileName() != null) {
                    fileParts.merge(part.getName(), 1, Integer::sum);
                }
            }
        }
        // Text parts and query parameters are not defined by these operations.
        request.getParameterMap().keySet().stream().findFirst().ifPresent(name -> {
            throw new InvalidRequestParameterException(name, "is not a defined parameter");
        });
        fileParts.forEach((name, count) -> {
            if (!definedFileParts.contains(name)) {
                throw new InvalidRequestParameterException(name, "is not a defined parameter");
            }
            if (count > 1) {
                throw new InvalidRequestParameterException(name, "must not be repeated");
            }
        });
    }

    private static Iterable<Part> parts(HttpServletRequest request) {
        try {
            return request.getParts();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ServletException e) {
            throw new IllegalStateException("Multipart request could not be read", e);
        }
    }
}
