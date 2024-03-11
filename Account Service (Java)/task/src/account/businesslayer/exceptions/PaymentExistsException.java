package account.businesslayer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class PaymentExistsException extends RuntimeException {
    public PaymentExistsException(String message) {
        super(message);
    }

}
