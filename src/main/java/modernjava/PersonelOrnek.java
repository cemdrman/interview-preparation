package modernjava;

import java.util.List;

public class PersonelOrnek {

    sealed interface Personel permits Doktor, Hemsire {
    }

    record Doktor(String isim, boolean operator) implements Personel {
    }

    record Hemsire(String isim) implements Personel {
    }

    public static void main(String[] args) {
        List<Personel> personeller = List.of(
                new Doktor("Ayşe", true),
                new Doktor("Mehmet", false),
                new Hemsire("Zeynep"));

        personeller.forEach(p -> System.out.println(gorev(p)));
        //Ayşe ameliyata girebilir
        //Mehmet sadece muayene yapar
        //Zeynep ameliyata girebilir

        System.out.println(eskiYontem(new Hemsire("Zeynep"))); //Zeynep
        System.out.println(yeniYontem(new Hemsire("Zeynep"))); //Zeynep
        System.out.println(yeniYontem("hemşire değil")); //tanımsız
    }

    // Java 21: pattern matching for switch + record pattern
    static String gorev(Personel personel) {
        return switch (personel) {
            case Doktor(String isim, boolean operator) when operator -> isim + " ameliyata girebilir";
            case Doktor d -> d.isim() + " sadece muayene yapar";
            case Hemsire h -> h.isim() + " ameliyata girebilir";
        };
    }

    // Java 16 öncesi
    static String eskiYontem(Object obj) {
        if (obj instanceof Hemsire) {
            Hemsire hemsire = (Hemsire) obj;
            return hemsire.isim();
        }
        return "tanımsız";
    }

    // Java 16: pattern matching for instanceof
    static String yeniYontem(Object obj) {
        if (!(obj instanceof Hemsire hemsire)) {
            return "tanımsız";
        }
        return hemsire.isim();
    }
}
