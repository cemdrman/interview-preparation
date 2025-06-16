package hospital;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String personalId; // TC kimlik no
    private List<Operation> operationCounter = new ArrayList<>(); // Operation listesi

    public String getPersonalId() {
        return personalId;
    }

    public void setPersonalId(String personalId) {
        this.personalId = personalId;
    }

    public List<Operation> getOperationCounter() {
        return operationCounter;
    }

    public void setOperationCounter(List<Operation> operationCounter) {
        this.operationCounter = operationCounter;
    }

    // Operation ekleme metodu
    public void addOperation(Operation operation) {
        operationCounter.add(operation);
    }
    public Customer(String personalId){
        this.personalId = personalId;
        Hospital.customers.add(this);

    }
}
