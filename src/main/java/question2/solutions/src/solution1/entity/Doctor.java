package solution1.entity;

public class Doctor {
    private Role role;
    private int operationFee;

    protected Doctor() {
        this.role = Role.DOCTOR;
    }

    public boolean isOperator() {
        return this.role == Role.OPERATOR;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getOperationFee() {
        return operationFee;
    }

    public void setOperationFee(int operationFee) {
        this.operationFee = operationFee;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "role=" + role.name() +
                ", operationFee=" + operationFee +
                "}";
    }

    public enum Role {
        OPERATOR, DOCTOR;
    }
}
