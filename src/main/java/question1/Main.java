package question1;

class Main {
    public static void main(String args[]) {
        PatikaInterview patikaInterview = new PatikaInterview(5);
    }
}

class Interview {
    Interview() {
        System.out.println("Mülakata hoş geldin");
    }
}

class PatikaInterview extends Interview {

    static {
        System.out.println("Patika.dev");
    }

    PatikaInterview() {
        System.out.println("Patika.dev ile mülakata hoş geldin");
    }

    PatikaInterview(int x) {
        super();
        System.out.println("Patika.dev ile mülakata hoş geldin x " + x);
    }
}