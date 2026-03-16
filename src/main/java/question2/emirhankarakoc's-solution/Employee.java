package hospital;


public class Employee {
    String name;
    EmployeeType type;
    int costPerOperation;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmployeeType getType() {
        return type;
    }

    public void setType(EmployeeType type) {
        this.type = type;
    }

    public int getCostPerOperation() {
        return costPerOperation;
    }

    public void setCostPerOperation(int costPerOperation) {
        this.costPerOperation = costPerOperation;
    }
}
