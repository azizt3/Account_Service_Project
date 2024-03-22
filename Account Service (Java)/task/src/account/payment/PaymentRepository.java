package account.payment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
    boolean existsByEmployeeAndPeriod(String employee, String period);
    Payment findByEmployeeAndPeriod(String employee, String period);
    List<Payment> findByEmployeeOrderByPeriodDesc(String employee);
}

