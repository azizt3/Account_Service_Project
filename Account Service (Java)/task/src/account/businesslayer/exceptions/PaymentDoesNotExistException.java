package account.businesslayer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class PaymentDoesNotExistException extends RuntimeException{
    public PaymentDoesNotExistException(String message) {
        super(message);
    }
}
