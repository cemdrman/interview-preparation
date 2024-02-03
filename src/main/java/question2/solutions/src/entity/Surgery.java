package entity;

import java.util.Arrays;

/**
 * Surgery class provides data and methods to perform an operation.
 * It contains doctors, nurses, instruments, the patient and surgery status.<br><br>
 * It can check if the operation can be performed, calculate the cost of the operation and perform the operation.<br><br>
 * Surgery status is a nested class that contains the success status of the operation and a message.<br><br>
 * Instrument is a nested class that contains the name and cost of the instrument.
 */
public class Surgery {
    private Doctor[] doctors;
    private Nurse[] nurses;
    private Patient patient;
    private Instrument[] instruments;
    private Status status;

    public Surgery(Doctor[] doctors, Nurse[] nurses, Patient patient, Instrument[] instruments) {
        this.doctors = doctors;
        this.nurses = nurses;
        this.patient = patient;
        this.instruments = instruments;
        this.status = new Status();
        isValid(); // to set Status
    }


    /**
     * Check if the surgery is valid (to perform an operation). An operation can be performed if the surgerical team is valid and the patient can undergo the operation.<br><br>
     * This method also sets the status of the surgery with the appropriate message.<br><br>
     * The status of the surgery can be checked with {@link Surgery.Status#getSuccessStatus}.<br>
     * The message of the surgery can be checked with {@link Surgery.Status#getMessage}.<br><br>
     * Surgery validity conditions are:<br>
     * - There must be at least 1 doctor and at most 5 nurses.<br>
     * - All doctors must be operators.<br>
     * - The patient should not have undergone an operation in the last year.
     *
     * @return true if the surgery is valid, false otherwise
     */
    public boolean isValid() {
        boolean valid = false;
        if (!isTeamValid()) status.invalidTeam();
        else if (!patient.canUndergoOperation()) status.invalidPatient();
        else valid = true;

        return valid;
    }

    /**
     * Check if the surgerical team is valid. A surgery can be performed if there is at least 1 doctor and at most 5 nurses AND all doctors are operators.
     *
     * @return true if the surgery is valid, false otherwise
     */
    public boolean isTeamValid() {
        return (doctors.length >= 1 && nurses.length <= 5) && Arrays.stream(doctors).allMatch(Doctor::isOperator);
    }

    /**
     * Calculate the cost of the operation. Behaviour of this method differs from {@link Surgery#calculateCostFixed}. Instead of using a shared cost per instrument, this method uses the cost of instruments individually.
     *
     * @return the cost of the operation
     */
    public int calculateCost() {
        // (instrument costs + doctor salaries)
        return Arrays.stream(instruments).mapToInt(Instrument::getCost).sum() + Arrays.stream(doctors).mapToInt(Doctor::getOperationFee).sum();
    }

    /**
     * Calculate the fixed cost of the operation. Uses the shared cost per instrument value from the Instrument class.
     *
     * @return the fixed cost of the operation
     */
    public int calculateCostFixed() {
        // (instrument amount * shared cost + doctor salaries)
        return calculateCostFixed(Instrument.sharedCostPerInstrument);
    }

    /**
     * Calculate the fixed cost of the operation. Instead of using the shared cost per instrument value from the Instrument class, it uses the related parameter.
     *
     * @param sharedCostPerInstrument the shared cost per instrument
     * @return the fixed cost of the operation
     */
    public int calculateCostFixed(int sharedCostPerInstrument) {
        // (instrument amount * shared cost) + doctor fee(s)
        return (instruments.length * sharedCostPerInstrument) + Arrays.stream(doctors).mapToInt(Doctor::getOperationFee).sum();
    }

    public Surgery performOperation() {
        if (!isValid()) {
            System.out.println("Operation cannot be performed. " + status.getMessage());
            return this;
        }
        // TODO
        // perform operation
        // change status accordingly
        return this;
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

    public Instrument[] getInstruments() {
        return instruments;
    }

    public void setInstruments(Instrument[] instruments) {
        this.instruments = instruments;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Surgery{" +
                "\n\tdoctors=" + Arrays.toString(doctors) +
                "\n\tnurses=" + Arrays.toString(nurses) +
                "\n\tpatient=" + patient +
                "\n\tinstruments=" + Arrays.toString(instruments) +
                "\n\tstatus=" + status +
                "\n\tvalidity=" + isValid() +
                "\n\tteamValidity=" + isTeamValid() +
                "\n\tcost=" + calculateCost() +
                "\n\tfixedCost=" + calculateCostFixed() +
                "\n}";
    }

    public static class Instrument {
        private String name;
        private int cost;
        private static int sharedCostPerInstrument;

        static {
            sharedCostPerInstrument = 100;
        }

        public Instrument(String name, int cost) {
            this.name = name;
            this.cost = cost;
        }

        public Instrument(String name, int cost, int sharedCostPerInstrument) {
            this.name = name;
            this.cost = cost;
            Instrument.sharedCostPerInstrument = sharedCostPerInstrument;
        }

        public String getName() {
            return name;
        }

        public int getCost() {
            return cost;
        }

        public static int getSharedCostPerInstrument() {
            return sharedCostPerInstrument;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        public static void setSharedCostPerInstrument(int sharedCostPerInstrument) {
            Instrument.sharedCostPerInstrument = sharedCostPerInstrument;
        }
    }

    public class Status {
        private SuccessStatus successStatus;
        private String message;

        public Status() {
            successStatus = SuccessStatus.UNKNOWN;
            message = "Operation status unknown. It generally means that the operation has not been performed yet.";
        }

        public void success() {
            successStatus = SuccessStatus.SUCCESS;
            message = "Operation successful.";
        }

        public void failure() {
            successStatus = SuccessStatus.FAILURE;
            message = "Operation failed.";
        }

        public void invalidTeam() {
            successStatus = SuccessStatus.INVALID_TEAM;
            message = "Invalid team. A surgery can be performed if there is at least 1 doctor and at most 5 nurses AND all doctors are operators.";
        }

        public void invalidPatient() {
            successStatus = SuccessStatus.INVALID_PATIENT;
            message = "Invalid patient. A patient can undergo at most 1 operation per year.";
        }

        public SuccessStatus getSuccessStatus() {
            return successStatus;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Status{" +
                    "successStatus=" + successStatus.name() +
                    ", message='" + message +
                    "}";
        }

        private enum SuccessStatus {
            SUCCESS, FAILURE, UNKNOWN, INVALID_TEAM, INVALID_PATIENT;
        }
    }
}
