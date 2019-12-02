package main;

// import java.util.Set;
// import java.util.List;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.Comparator;

public class Main {
    public static void main(String[] args) {
        String filename = args[0];
        int minsup = Integer.parseInt(args[1]);
        float minconf = Float.parseFloat(args[2]);

        // System.out.println("\nfile:" + filename + "\nminsup:" + minsup + "\nminconf:"
        // + minconf);

        // Set<Rule> unorderedRules =
        new ExploreConcepts(filename, minsup, minconf, 0).run();
        // List<Rule> rules = new ArrayList<Rule>(unorderedRules);
        // // リフト順に並び替え
        // Collections.sort(rules, new Comparator<Rule>() {
        // @Override
        // public int compare(Rule r0, Rule r1) {
        // return -Float.compare(r0.getStat().getLift(), r1.getStat().getLift());
        // }
        // });

        // for (Rule elm : rules) {
        // System.out.println(elm);
        // }
    }
}