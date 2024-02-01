package question1;

import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

public class CollectionsMain {

    public static void main(String[] args) {

        List<String> nameList = List.of("elif", "emir", "cem", "nehir", "melike", "melek", "elif");

        System.out.println("size: " + nameList.size() + " ->" + nameList);

        TreeSet<String> treeSet = new TreeSet<>(nameList);

        System.out.println("size: " + treeSet.size() + " ->" + treeSet);

        HashSet<String> hashSet = new HashSet<>(nameList);

        System.out.println("size: " + hashSet.size() + " ->" + hashSet);
    }
}
