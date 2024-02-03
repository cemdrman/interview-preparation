package entity;

public class Patient {
    private int operationsReceived;
    private int reservationsMade;

    public Patient() {
        operationsReceived = 0;
        reservationsMade = 0;
    }

    public Patient(int operationsReceived, int reservationsMade) {
        this.operationsReceived = operationsReceived;
        this.reservationsMade = reservationsMade;
    }

    public int getReservationsMade() {
        return reservationsMade;
    }

    public boolean canUndergoOperation() {
        return operationsReceived < 1;
    }

    public boolean canMakeReservation() {
        return reservationsMade <= 3;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "operationsReceived=" + operationsReceived +
                ", reservationsMade=" + reservationsMade +
                "}";
    }
}
