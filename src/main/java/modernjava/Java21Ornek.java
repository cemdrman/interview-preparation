package modernjava;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Java21Ornek {

    public static void main(String[] args) {
        sequencedCollectionOrnek();
        virtualThreadOrnek();
    }

    static void sequencedCollectionOrnek() {
        List<String> nameList = new ArrayList<>(List.of("elif", "emir", "cem"));

        // Java 21 öncesi: nameList.get(nameList.size() - 1)
        System.out.println(nameList.getFirst()); //elif
        System.out.println(nameList.getLast()); //cem
        System.out.println(nameList.reversed()); //[cem, emir, elif]

        LinkedHashSet<String> nameSet = new LinkedHashSet<>(nameList);
        nameSet.addFirst("nehir");
        System.out.println(nameSet); //[nehir, elif, emir, cem]
    }

    static void virtualThreadOrnek() {
        long baslangic = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10_000).forEach(i -> executor.submit(() -> {
                Thread.sleep(Duration.ofSeconds(1));
                return i;
            }));
        }

        System.out.println("10.000 görev " + (System.currentTimeMillis() - baslangic) + " ms sürdü");
    }
}
