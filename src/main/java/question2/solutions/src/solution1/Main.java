package solution1;

import solution1.entity.*;

public class Main {
    public static void main(String[] args) {
        Doctor[] doctors = {
                DoctorBuilder.start().operator(8000).build(),
                DoctorBuilder.start().build(),
                DoctorBuilder.start().build(),
                DoctorBuilder.start().build(),
                DoctorBuilder.start().operator(5000).build()
        };
        Nurse[] nurses = {new Nurse(), new Nurse(), new Nurse(), new Nurse(), new Nurse()};
        Patient[] patients = {new Patient(1, 2), new Patient(0, 3), new Patient(1, 3), new Patient(1, 1), new Patient(0, 2), new Patient(1, 3), new Patient(1, 2), new Patient()};
        Surgery.Instrument[] instruments = {new Surgery.Instrument("Scalpel", 500), new Surgery.Instrument("Forceps", 185), new Surgery.Instrument("Needle", 25)};

        Hospital hospital = new Hospital(doctors, nurses, patients);

        Surgery surgery = new Surgery(new Doctor[]{doctors[0]}, nurses, patients[0], instruments);
        Surgery surgery2 = new Surgery(new Doctor[]{doctors[1]}, nurses, patients[1], instruments);
        Surgery surger3 = new Surgery(new Doctor[]{doctors[0]}, nurses, patients[1], instruments);
        Surgery surger4 = new Surgery(new Doctor[]{doctors[4]}, nurses, patients[1], instruments);
        hospital.setSurgeries(new Surgery[]{surgery, surgery2, surger3, surger4});

        System.out.println("Top visiter: " + hospital.findTopVisiter());
        System.out.println("Highest Cost for an Operation: " + hospital.findHighestCostForAnOperation());
        System.out.println("Highest Fixed Cost for an Operation: " + hospital.findHighestFixedCostForAnOperation());
        System.out.println("\nSurgery Details:\n" + surgery);
        System.out.println("\nSurgery2 Details:\n" + surgery2);
        System.out.println("\nSurgery3 Details:\n" + surger3);
        System.out.println("\nSurgery4 Details:\n" + surger4);
    }
}