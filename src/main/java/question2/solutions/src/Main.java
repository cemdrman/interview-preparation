import entity.Doctor;
import entity.Hospital;
import entity.Nurse;
import entity.Patient;
import entity.Surgery;

public class Main {
    public static void main(String[] args) {
        Doctor[] doctors = {new Doctor(Doctor.Role.OPERATOR), new Doctor(Doctor.Role.DOCTOR), new Doctor(Doctor.Role.DOCTOR), new Doctor(Doctor.Role.DOCTOR), new Doctor(Doctor.Role.OPERATOR)};
        Nurse[] nurses = {new Nurse(), new Nurse(), new Nurse(), new Nurse(), new Nurse()};
        Patient[] patients = {new Patient(1, 2), new Patient(0, 3), new Patient(1, 3), new Patient(1, 1), new Patient(0, 2), new Patient(1, 3), new Patient(1, 2), new Patient()};
        Surgery.Instrument[] instruments = {new Surgery.Instrument("Scalpel", 100), new Surgery.Instrument("Forceps", 50), new Surgery.Instrument("Needle", 25)};

        Hospital hospital = new Hospital(doctors, nurses, patients);
        Surgery surgery = new Surgery(new Doctor[]{doctors[0]}, nurses, patients[0], instruments);
        Surgery surgery2 = new Surgery(new Doctor[]{doctors[1]}, nurses, patients[1], instruments);

        System.out.println(hospital.findTopVisiter());
        System.out.println(surgery);
        System.out.println(surgery2);
    }
}