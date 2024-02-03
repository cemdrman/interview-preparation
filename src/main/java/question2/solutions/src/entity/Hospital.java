package entity;

import java.util.Arrays;

public class Hospital {
    private Doctor[] doctors;
    private Nurse[] nurses;
    private Patient[] patients;

    public Hospital(Doctor[] doctors, Nurse[] nurses, Patient[] patients) {
        this.doctors = doctors;
        this.nurses = nurses;
        this.patients = patients;
    }

    public Patient findTopVisiter() {
        Object[] sorted = Arrays.stream(patients).sorted(
                (patient, patient2) -> {
                    if (patient.getReservationsMade() < patient2.getReservationsMade()) return -1;
                    else if (patient.getReservationsMade() > patient2.getReservationsMade()) return 1;
                    else return 0;
                }).toArray();
        return (Patient) sorted[patients.length - 1];
    }
}

