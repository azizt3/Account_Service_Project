package account.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentPostedDto(@NotBlank String status) {

}
