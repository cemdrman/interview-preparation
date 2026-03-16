package hospital;

import java.util.ArrayList;
import java.util.List;

public class Hospital {
    public static List<Customer> customers = new ArrayList<>();
    public static List<Operation> operations = new ArrayList<>();
    public static void main(String[] args) {
        // Doktorlar
        Doctor dr1 = new Doctor(EmployeeType.OPERATOR, "EMIRHAN", 300);
        Doctor dr2 = new Doctor(EmployeeType.OPERATOR, "AHMET", 350);
        Doctor dr3 = new Doctor(EmployeeType.OPERATOR, "MEHMET", 320);
        Doctor dr4 = new Doctor(EmployeeType.OPERATOR, "AYSE", 310);
        Doctor dr5 = new Doctor(EmployeeType.OPERATOR, "FATMA", 330);
        // Hemşireler
        Nurse nurse1 = new Nurse("SILA");
        Nurse nurse2 = new Nurse("MERVE");
        Nurse nurse3 = new Nurse("DENIZ");
        Nurse nurse4 = new Nurse("BERIL");
        Nurse nurse5 = new Nurse("YUSUF");
        // Müşteriler
        Customer customer1 = new Customer("22255566677");
        Customer customer2 = new Customer("12345678910");
        // Operasyonlar
        Operation operation1 = new Operation(400, customer1, dr1, nurse1);
        Operation operation2 = new Operation(500, customer2, dr2, nurse2);
        Operation operation3 = new Operation(600, customer1, dr3, nurse3);
        Operation operation4 = new Operation(700, customer2, dr4, nurse4);
        Operation operation5 = new Operation(800, customer1, dr5, nurse5);
        Operation operation6 = new Operation(900, customer2, dr1, nurse1);
        Operation operation7 = new Operation(1000, customer1, dr2, nurse2);
        Operation operation8 = new Operation(1100, customer2, dr3, nurse3);
        Operation operation9 = new Operation(1200, customer1, dr4, nurse4);
        Operation operation10 = new Operation(1300, customer2, dr5, nurse5);
        Operation operation11 = new Operation(1300, customer2, dr5, nurse5);
        Operation operation12 = new Operation(1300, customer2, dr5, nurse5);

        System.out.println("Most operated person's personal Id: "+maxOperationCount(Hospital.customers));
        System.out.println("Highest operation cost: "+maxOperationCost(Hospital.operations));
    }


    public static String maxOperationCount(List<Customer> customerList){
        int highest = 0;

        //en cok amelyat olan adamin kac kere amelyat oldugunu bul.
        for (Customer customer : customerList) {
            if (highest <= customer.getOperationCounter().size()) {
                highest = customer.getOperationCounter().size();
            }
        }
        //en cok amelyat olan kisinin, amelyat olma sayisi ile kimin amelyat olma sayisi denk onu bul , o kisiyi donder.
        for (Customer customer : customerList) {
            if (customer.getOperationCounter().size() == highest) return customer.getPersonalId();
        }
        return null;
    }

    public static int maxOperationCost(List<Operation> operationList){
        int highest = 0;
        for (Operation operation : operationList) {
            if (highest <= operation.getCost()) {
                highest = operation.getCost();
            }
        }
        return highest;
    }


}
