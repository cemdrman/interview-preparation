package hospital;



public class Doctor extends Employee {
    public Doctor(EmployeeType type, String name,int operationCost){
        if (type.equals(EmployeeType.NURSE)) throw new RuntimeException("Any doctor cannot be saved as Nurse");

        this.type = type;
        this.name = name;
        this.costPerOperation = operationCost;
    }
}
