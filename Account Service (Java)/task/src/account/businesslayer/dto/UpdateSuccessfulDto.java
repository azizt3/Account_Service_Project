package account.businesslayer.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSuccessfulDto(@NotBlank String email, String status) {
}
