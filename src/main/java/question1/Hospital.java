package question1;

class Hospital {

    private static int doctors;
    public final static int NURSES = 10;
    protected double salaries;

    private Hospital() {
    }

    public Hospital(int doctors, int nurses) {
        this.doctors = doctors;
        //this.NURSES = nurses;
    }

    public Hospital(int doctors) {
        this.doctors = doctors;
    }

    public Hospital(double salaries) {
        this.salaries = salaries;
    }
}
