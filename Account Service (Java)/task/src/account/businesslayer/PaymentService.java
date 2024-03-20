package account.businesslayer;

import account.businesslayer.dto.PaymentDto;
import account.businesslayer.dto.PaymentPostedDto;
import account.businesslayer.dto.UserAdapter;
import account.businesslayer.entity.Payment;
import account.businesslayer.exceptions.InvalidPaymentException;
import account.businesslayer.exceptions.PaymentDoesNotExistException;
import account.businesslayer.exceptions.PaymentExistsException;
import account.businesslayer.request.PaymentAddRequest;
import account.persistencelayer.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PaymentService {

    PaymentRepository paymentRepository;
    UserService userService;

    @Autowired
    public PaymentService (PaymentRepository paymentRepository, UserService userService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    //Business logic
    public ResponseEntity<PaymentDto> handleGetPayment(String period, UserAdapter user) throws ParseException {
        userService.validateUserExists(user.getEmail());
        Payment payment = paymentRepository.findByEmployeeAndPeriod(user.getEmail().toLowerCase(), period);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildPaymentDto(payment, user));
    }

    public ResponseEntity<PaymentDto[]> handleGetAllPayments(UserAdapter user) throws ParseException{
        userService.validateUserExists(user.getEmail());
        List<Payment> allPayments = paymentRepository.findByEmployeeOrderByPeriodDesc(user.getEmail().toLowerCase());
        PaymentDto[] paymentDto = allPayments.stream()
                .map(payment -> buildPaymentDto(payment, user))
                .toList().toArray(new PaymentDto[0]);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentDto);
    }

    @Transactional
    public Payment postPayment (PaymentAddRequest payment) {
        Payment postedPayment = new Payment(payment.employee(), payment.period(), payment.salary());
        paymentRepository.save(postedPayment);
        return postedPayment;
    }

    @Transactional
    public ResponseEntity<PaymentPostedDto> updatePayment (PaymentAddRequest payment) {
        userService.validateUserExists(payment.employee());
        validatePaymentPositive(payment.salary());
        Payment updatedPayment = paymentRepository.findByEmployeeAndPeriod(payment.employee(), payment.period());
        updatedPayment.setSalary(payment.salary());
        paymentRepository.save(updatedPayment);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PaymentPostedDto("Added successfully!"));
    }

    public PaymentDto buildPaymentDto(Payment payment, UserAdapter user) {
        return new PaymentDto(
                user.getName(),
                user.getLastName(),
                formatPeriod(payment.getPeriod()),
                formatSalary(payment.getSalary())
        );
    }

    //Formatting Methods

    public String formatSalary(Long cents) {
        Long change = cents%100;
        Long dollars = (cents - change)/100;
        return dollars + " dollar(s) " + change + " cent(s)";
    }

    public String formatPeriod (String period) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("MM-yyyy");
        SimpleDateFormat targetFormat = new SimpleDateFormat("MMMM-yyyy");
        try {
            Date date = originalFormat.parse(period);
            return targetFormat.format(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    //Validation Methods

    public void validatePaymentAdd(PaymentAddRequest payment){
        validatePaymentPositive(payment.salary());
        validateUniquePayment(payment);
        userService.validateUserExists(payment.employee());
    }

    public void validateUniquePayment(PaymentAddRequest payment) {
        if (paymentRepository.existsByEmployeeAndPeriod(payment.employee(), payment.period())) {
            throw new PaymentExistsException("Cannot add duplicate payment");
        }
    }
    public void validatePaymentPositive(Long payment) {
        if (payment < 0) throw new InvalidPaymentException("Salary cannot be negative!");
    }
}
