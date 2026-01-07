package api.vybe.util;

import java.util.regex.Pattern;

public class ValidityUtil {

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }
    public static boolean isValidPhone(String phone) {
        String phoneRegex = "^998[0-9]{9}$";// Uzbekistan format: +998 followed by 9 digits
        return Pattern.matches(phoneRegex, phone);
    }

}
