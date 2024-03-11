package account.businesslayer.exceptions;

import io.micrometer.core.instrument.config.validate.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message){super(message);}
}
