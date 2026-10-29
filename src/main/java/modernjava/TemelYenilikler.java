package modernjava;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TemelYenilikler {

    public static void main(String[] args) {
        varOrnek();
        collectionFactoryOrnek();
        stringOrnek();
        switchOrnek("CUMARTESI");
        switchOrnek("CUMA");
        textBlockOrnek();
    }

    // Java 10
    static void varOrnek() {
        var isimler = new ArrayList<String>();
        isimler.add("cem");
        //isimler.add(5); // does not compile
        //var x; // does not compile
        //var y = null; // does not compile
        System.out.println(isimler.getClass().getSimpleName()); //ArrayList
    }

    // Java 9
    static void collectionFactoryOrnek() {
        List<String> nameList = List.of("elif", "emir", "elif");
        System.out.println(nameList); //[elif, emir, elif]

        try {
            nameList.add("cem");
        } catch (UnsupportedOperationException e) {
            System.out.println("List.of immutable'dır");
        }

        try {
            Set.of("elif", "emir", "elif");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); //duplicate element: elif
        }
    }

    // Java 11
    static void stringOrnek() {
        System.out.println("   ".isBlank()); //true
        System.out.println("   ".isEmpty()); //false

        String bosluklu = " cem ";
        System.out.println(bosluklu.trim().length()); //5
        System.out.println(bosluklu.strip().length()); //3

        System.out.println("ab".repeat(3)); //ababab
        System.out.println("satir1\nsatir2\nsatir3".lines().count()); //3
    }

    // Java 14
    static void switchOrnek(String gun) {
        int calismaSaati = switch (gun) {
            case "CUMARTESI", "PAZAR" -> 0;
            case "CUMA" -> {
                System.out.println("hafta sonu yaklaşıyor");
                yield 6;
            }
            default -> 8;
        };
        System.out.println(gun + " -> " + calismaSaati);
    }

    // Java 15
    static void textBlockOrnek() {
        String json = """
                {
                  "isim": "cem",
                  "dil": "java"
                }
                """;
        System.out.print(json);
    }
}
