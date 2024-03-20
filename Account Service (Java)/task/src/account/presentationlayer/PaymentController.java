package account.presentationlayer;

import account.businesslayer.PaymentService;
import account.businesslayer.dto.UserAdapter;
import account.businesslayer.dto.PaymentDto;
import account.businesslayer.dto.PaymentPostedDto;
import account.businesslayer.request.PaymentAddRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

@RestController
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    public PaymentController (PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @GetMapping(path = "/api/empl/payment")
    public ResponseEntity<?> getPayment (
        @RequestParam(required = false) @Pattern(regexp = "(0[1-9]|1[1,2])-(19|20)\\d{2}") Optional<String> period,
        @AuthenticationPrincipal UserAdapter user) throws ParseException {

        if (period.isPresent()){
            return paymentService.handleGetPayment(period.get(), user);
        }
        return paymentService.handleGetAllPayments(user);
    }

    @PutMapping(path = "/api/acct/payments")
    public ResponseEntity<?> updatePayments(@RequestBody PaymentAddRequest payment) {
        return paymentService.updatePayment(payment);
    }

    @PostMapping(path = "/api/acct/payments")
    public ResponseEntity<?> addPayments(
        @NotEmpty(message = "Payments cannot be empty") @RequestBody List<@Valid PaymentAddRequest> payments) {
            payments.forEach(paymentService::validatePaymentAdd);
            payments.forEach(paymentService::postPayment);

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body( new PaymentPostedDto("Added successfully!"));
    }
}
