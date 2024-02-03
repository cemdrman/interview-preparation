package solution1.entity;

public class DoctorBuilder {
    private Doctor doctor;

    private DoctorBuilder() {
        this.doctor = new Doctor();
    }

    public static DoctorBuilder start() {
        return new DoctorBuilder();
    }

    public DoctorBuilder operator() {
        doctor.setRole(Doctor.Role.OPERATOR);
        doctor.setOperationFee(1000);
        return this;
    }

    public DoctorBuilder operator(int operationFee) {
        doctor.setRole(Doctor.Role.OPERATOR);
        doctor.setOperationFee(operationFee);
        return this;
    }

    public Doctor build() {
        return doctor;
    }
}
