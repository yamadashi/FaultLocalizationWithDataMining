import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filename = args[0];
        int minsup = Integer.parseInt(args[1]);
        float minconf = Float.parseFloat(args[2]);

        System.out.println("\nfile:" + filename + "\nminsup:" + minsup + "\nminconf:" + minconf);

        List<Pair<Concept, Pair<Integer, Float>>> solution = new ExploreConcepts(filename, minsup, minconf, 0).solve();
        System.out.println("=============================");
        for (Pair<Concept, Pair<Integer, Float>> elm : solution) {
            Pair<Integer, Float> info = elm.getSecond();
            System.out.print("sup:" + info.getFirst() + " conf:" + info.getSecond() + " ");
            elm.getFirst().print();
        }
    }
}