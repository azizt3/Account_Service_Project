package account;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BreachedPasswords {

    private final List<String> breachedPasswords;

    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    public BreachedPasswords(List<String> breachedPasswords) {
        List<String> passwords = List.of(
            "PasswordForJanuary",
            "PasswordForFebruary",
            "PasswordForMarch",
            "PasswordForApril",
            "PasswordForMay",
            "PasswordForJune",
            "PasswordForJuly",
            "PasswordForAugust",
            "PasswordForSeptember",
            "PasswordForOctober",
            "PasswordForNovember",
            "PasswordForDecember"
        );
        this.breachedPasswords =  passwords.stream()
            .map(password -> passwordEncoder().encode(password))
            .toList();
    }

    public List<String> getBreachedPasswords() {
        return breachedPasswords;
    }
}
