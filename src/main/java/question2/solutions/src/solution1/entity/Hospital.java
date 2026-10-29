package solution1.entity;

import java.util.Arrays;

public class Hospital {
    private Doctor[] doctors;
    private Nurse[] nurses;
    private Patient[] patients;
    private Surgery[] surgeries;

    public Hospital(Doctor[] doctors, Nurse[] nurses, Patient[] patients) {
        this.doctors = doctors;
        this.nurses = nurses;
        this.patients = patients;
    }

    public Patient findTopVisiter() {
        if (patients.length == 0) return null;

        Object[] sorted = Arrays.stream(patients).sorted(
                (patient, patient2) -> {
                    if (patient.getReservationsMade() < patient2.getReservationsMade()) return -1;
                    else if (patient.getReservationsMade() > patient2.getReservationsMade()) return 1;
                    else return 0;
                }).toArray();
        return (Patient) sorted[patients.length - 1];
    }

    public int findHighestCostForAnOperation() {
        if (surgeries.length == 0) return -1;

        Object[] sorted = Arrays.stream(surgeries)
                .filter(Surgery::isValid)
                .sorted(
                        (surgery, surgery2) -> {
                            if (surgery.calculateCost() < surgery2.calculateCost()) return -1;
                            else if (surgery.calculateCost() > surgery2.calculateCost()) return 1;
                            else return 0;
                        }).toArray();
        if (sorted.length == 0) return -1;
        return ((Surgery) sorted[sorted.length - 1]).calculateCost();
    }

    public int findHighestFixedCostForAnOperation() {
        if (surgeries.length == 0) return -1;

        Object[] sorted = Arrays.stream(surgeries)
                .filter(Surgery::isValid)
                .sorted(
                        (surgery, surgery2) -> {
                            if (surgery.calculateCostFixed() < surgery2.calculateCostFixed()) return -1;
                            else if (surgery.calculateCostFixed() > surgery2.calculateCostFixed()) return 1;
                            else return 0;
                        }).toArray();
        if (sorted.length == 0) return -1;
        return ((Surgery) sorted[sorted.length - 1]).calculateCostFixed();
    }

    public Doctor[] getDoctors() {
        return doctors;
    }

    public void setDoctors(Doctor[] doctors) {
        this.doctors = doctors;
    }

    public Nurse[] getNurses() {
        return nurses;
    }

    public void setNurses(Nurse[] nurses) {
        this.nurses = nurses;
    }

    public Patient[] getPatients() {
        return patients;
    }

    public void setPatients(Patient[] patients) {
        this.patients = patients;
    }

    public Surgery[] getSurgeries() {
        return surgeries;
    }

    public void setSurgeries(Surgery[] surgeries) {
        this.surgeries = surgeries;
    }
}

