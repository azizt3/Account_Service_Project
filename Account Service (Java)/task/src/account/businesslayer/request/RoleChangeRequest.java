package account.businesslayer.request;

import jakarta.validation.constraints.NotBlank;

public record RoleChangeRequest(@NotBlank String user, @NotBlank String role, @NotBlank String operation) {
}
