package hospital;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Operation {
    private List<Employee> employeeList = new ArrayList<>();
    private int cost = 0;
    private LocalDate operationDate = LocalDate.now();

    private Customer customer;

    public Operation(int itemCosts, Customer customer, Employee... employees) {
        int operatorDoctorCounter = 0;
        int nurseCounter = 0;
        for (Employee employee : employees) {
            if (employee.getType() == EmployeeType.OPERATOR) {
                operatorDoctorCounter++;
                this.cost += employee.getCostPerOperation();
            }
            if (employee.getType() != EmployeeType.NURSE) {
                nurseCounter++;
            }
        }
        if (!(operatorDoctorCounter > 0 && nurseCounter <= 5)) {
            throw new RuntimeException("Exception message");
        }
        employeeList.addAll(Arrays.asList(employees));
        this.cost += itemCosts;
        this.customer = customer;
        this.customer.addOperation(this); // Müşterinin operasyon sayacına bu operasyonu ekliyoruz
        Hospital.operations.add(this);
    }

    // Getter ve setter metodları
    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public LocalDate getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(LocalDate operationDate) {
        this.operationDate = operationDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
