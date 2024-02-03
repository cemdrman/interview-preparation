package entity;

public class Doctor {
    private Role role;
    private int operationFee;
    
    public Doctor(Role role) {
        this.role = role;
    }

    public int getOperationFee() {
        return operationFee;
    }

    public void setOperationFee(int operationFee) {
        this.operationFee = operationFee;
    }

    public boolean isOperator() {
        return this.role == Role.OPERATOR;
    }

    public enum Role {
        OPERATOR, DOCTOR;
    }
}
