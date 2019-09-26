import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filename = args[0];
        int minsup = Integer.parseInt(args[1]);
        float minconf = Float.parseFloat(args[2]);

        System.out.println("\nfile:" + filename + "\nminsup:" + minsup + "\nminconf:" + minconf);

        List<Concept> solution = new ExploreConcepts(filename, minsup, minconf, 0).solve();
        System.out.println("=============================");
        for (Concept elm : solution) {
            System.out.println("(" + elm.getStat() + ") " + elm);
        }
    }
}