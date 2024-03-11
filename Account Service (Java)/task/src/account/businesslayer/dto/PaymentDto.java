package account.businesslayer.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentDto( String name, String lastname, String period,  String salary) {
}
