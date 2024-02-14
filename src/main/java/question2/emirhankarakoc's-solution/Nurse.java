package hospital;

import static hospital.EmployeeType.NURSE;

public class Nurse extends Employee {
    public Nurse(String name){
        this.name = name;
        this.type = NURSE;
        this.costPerOperation = 50;
    }
}
