package account.businesslayer.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InsufficientPasswordException extends RuntimeException {

    public InsufficientPasswordException(String message) {
        super(message);
    }
}
