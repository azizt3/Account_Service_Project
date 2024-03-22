package account.payment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "payments")
//@IdClass(PaymentId.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    @Column(name = "employee")
    @NotBlank
    private String employee;

    @Column(name = "period")
    @NotBlank
    @Pattern(regexp = "(0[1-9]|1[1,2])-(19|20)\\d{2}", message = "Invalid date!")
    private String period;

    @Column(name = "salary")
    private Long salary;

    public Payment(){}

    public Payment(String employee, String period, Long salary){
        this.employee = employee;
        this.period = period;
        this.salary = salary;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }
}
