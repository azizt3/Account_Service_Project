package account.businesslayer.dto;

import jakarta.validation.constraints.NotBlank;

public record UserDeletedDto(@NotBlank String user, String status) {
}
