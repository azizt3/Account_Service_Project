package account.businesslayer.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentPostedDto(@NotBlank String status) {

}
