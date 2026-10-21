package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidPhoneNumberException;

import java.util.regex.Pattern;

public record PhoneNumber(String number, String extension) {

    private static final Pattern DIGITS_10 = Pattern.compile("^[0-9]{10}$");

    public PhoneNumber {
        if (number == null || !DIGITS_10.matcher(number).matches()) {
            throw new InvalidPhoneNumberException(
                "Phone number must contain exactly 10 digits, got: " + number);
        }
        if (extension != null && extension.length() > 5) {
            throw new InvalidPhoneNumberException(
                "Phone extension must not exceed 5 characters, got: " + extension.length());
        }
    }
}
