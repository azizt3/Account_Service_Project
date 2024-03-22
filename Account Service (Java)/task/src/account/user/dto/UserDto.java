package account.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserDto(
    @NotBlank Long id,
    @NotBlank String name,
    @NotBlank String lastname,
    @NotBlank String email,
    @NotBlank String[] roles) {
}
