package account.businesslayer.request;

import jakarta.validation.constraints.NotBlank;

public record RoleChangeRequest(@NotBlank String user, @NotBlank String role, @NotBlank String operation) {

    public RoleChangeRequest(String user, String role, String operation){
        this.user = user;
        this.operation=operation;
        this.role = "ROLE_" + role;
    }

    }



