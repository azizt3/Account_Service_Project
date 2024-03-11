package account.businesslayer;

import account.businesslayer.dto.PaymentDto;
import account.businesslayer.dto.UserAdapter;
import account.businesslayer.entity.Payment;
import account.businesslayer.exceptions.InvalidPaymentException;
import account.businesslayer.exceptions.PaymentDoesNotExistException;
import account.businesslayer.exceptions.PaymentExistsException;
import account.businesslayer.request.PaymentAddRequest;
import account.persistencelayer.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    UserService userService;

    public PaymentService (PaymentRepository paymentRepository, UserService userService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    //Validation Methods
    public void validatePaymentAdd(PaymentAddRequest payment){
        validatePaymentAmount(payment);
        validateDuplicatePayment(payment);
        userService.validateUserExists(payment.employee());
    }
    public void validatePaymentUpdate(PaymentAddRequest payment) {
        validatePaymentAmount(payment);
        userService.validateUserExists(payment.employee());
    }
    public void validateDuplicatePayment (PaymentAddRequest payment) {
        if (paymentRepository.existsByEmployeeAndPeriod(payment.employee(), payment.period())) {
            throw new PaymentExistsException("Cannot add duplicate payment");
        }
    }
    public void validatePaymentAmount(PaymentAddRequest payment) {
        if (payment.salary() < 0) throw new InvalidPaymentException("Salary cannot be negative!");
    }

    private void validatePaymentExists(String period, UserAdapter user) {
        if (!paymentRepository.existsByEmployeeAndPeriod(user.getEmail().toLowerCase(), period)){
            throw new PaymentDoesNotExistException("Pay period does not exist!");
        }
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

    //Business logic

    public PaymentDto handleGetPayment(String period, UserAdapter user) throws ParseException {
        userService.validateUserExists(user.getEmail());
        Payment payment = paymentRepository.findByEmployeeAndPeriod(user.getEmail().toLowerCase(), period)
            .orElseThrow(() -> new PaymentDoesNotExistException("Payment does not Exist!"));
        return new PaymentDto(user.getName(), user.getLastName(), formatPeriod(period), formatSalary(payment.getSalary()));
    }

    public PaymentDto[] handleGetAllPayments(UserAdapter user) throws ParseException{
        userService.validateUserExists(user.getEmail());
        List<Payment> allPayments = paymentRepository.findByEmployeeOrderByPeriodDesc(user.getEmail().toLowerCase());
        List<PaymentDto> payments =  allPayments.stream()
            .map(payment -> new PaymentDto(
                user.getName(),
                user.getLastName(),
                formatPeriod(payment.getPeriod()),
                formatSalary(payment.getSalary())))
            .toList();
        return payments.toArray(new PaymentDto[0]);
    }

    @Transactional
    public Payment postPayment (PaymentAddRequest payment) {
        Payment postedPayment = new Payment(payment.employee(), payment.period(), payment.salary());
        paymentRepository.save(postedPayment);
        return postedPayment;
    }

    @Transactional
    public Payment updatePayment (PaymentAddRequest payment) {
        Payment updatedPayment = paymentRepository.findByEmployeeAndPeriod(payment.employee(), payment.period())
            .orElseThrow(() -> new PaymentDoesNotExistException("There is no existing payment to update!"));
        updatedPayment.setSalary(payment.salary());
        return updatedPayment;
    }
}
