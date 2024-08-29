import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void iterateOverList(ListIterator<String> iter) {
        // Iterate over the list
        while (iter.hasNext()) {
            String current = iter.next();
            if (current.equals("Hip")) {
                // Add "Hop" after "Hip"
                iter.add("Hop");
            }
        }
    }

    public static void printList(ListIterator<String> iter) {
        // Iterate over the list and print each element
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }

    /* Do not change code below */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> list = Arrays.stream(scanner.nextLine().split(" ")).collect(Collectors.toList());
        iterateOverList(list.listIterator());
        printList(list.listIterator());
    }
}
