package account.businesslayer.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRegistrationRequest(
    @NotBlank String name,
    @NotBlank String lastname,
    @NotBlank @Email @Pattern(regexp = "^(.+)@acme.com$") String email,
    @NotBlank String password) {
}
