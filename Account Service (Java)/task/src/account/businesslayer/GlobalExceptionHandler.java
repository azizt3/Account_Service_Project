package account.businesslayer;

import account.businesslayer.exceptions.*;
import account.businesslayer.response.CustomErrorMessage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.h2.command.dml.Set;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(buildErrorMessage(extractValidationMessage(ex), request), status);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomErrorMessage> handleConstraintViolationException(
        ConstraintViolationException ex, WebRequest request) {
        var violations = ex.getConstraintViolations();
        StringBuilder builder = new StringBuilder();
        String errorMessage = violations.stream()
            .map(violation -> builder.append(" " + violation.getMessage()))
            .toString();
        return new ResponseEntity<>(buildErrorMessage(errorMessage, request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientPasswordException.class)
    public ResponseEntity<CustomErrorMessage> handleInsufficientPasswordException(
        InsufficientPasswordException ex, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(buildErrorMessage(ex.getMessage(), request), status);
    }

    @ExceptionHandler(PaymentDoesNotExistException.class)
    public ResponseEntity<CustomErrorMessage> handlePaymentDoesNotExistException(
        PaymentDoesNotExistException ex, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(buildErrorMessage(ex.getMessage(), request), status);
    }

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<CustomErrorMessage> handleUserExistsException(
        UserExistsException ex, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(buildErrorMessage(ex.getMessage(), request), status);
    }

    @ExceptionHandler(InvalidChangeException.class)
    public ResponseEntity<CustomErrorMessage> handleInvalidChangeException(
        InvalidChangeException ex, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(buildErrorMessage(ex.getMessage(), request), status);
    }


    @ExceptionHandler(PaymentExistsException.class)
    public ResponseEntity<CustomErrorMessage> handlePaymentExistsException(
        PaymentExistsException ex, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(buildErrorMessage(ex.getMessage(), request), status);
    }

    @ExceptionHandler(AuthorizationViolationException.class)
    public ResponseEntity<CustomErrorMessage> handleAuthorizationViolationException(
        AuthorizationViolationException ex, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(buildErrorMessage(ex.getMessage(), request), status);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomErrorMessage> handleNotFoundException(
        NotFoundException ex, HttpStatus status, WebRequest request){
        return new ResponseEntity<>(buildErrorMessage(ex.getMessage(), request), status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException (HttpServletResponse response) throws IOException {
        response.sendError(403, "Access Denied!");
    }

    private String extractValidationMessage(Exception ex) {
        String exceptionMessage = ex.getMessage();
        String[] messageParts = exceptionMessage.split(";");
        String validationMessage = messageParts[messageParts.length - 1];
        return validationMessage.trim().replaceAll("default message \\[|]]", "");
    }

    private CustomErrorMessage buildErrorMessage(String errorMessage, WebRequest request) {
        return new CustomErrorMessage(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            errorMessage,
            request.getDescription(false).trim().replaceAll("uri=", ""));
    }

}
