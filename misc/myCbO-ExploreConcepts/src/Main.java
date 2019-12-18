import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String filename = args[0];
        int minsup = Integer.parseInt(args[1]);
        float minconf = Float.parseFloat(args[2]);
        int targetIndex = 0; // ルールの後件となる属性のインデックス
        int negObjIndex = 1; // targetIndex属性の反対属性のインデックス
        String ruleMiner = "";
        // ruleMiner = "Cellier"; // yamada20191114版
        ruleMiner = "cbo"; // cbo版(depth-first, ppc拡大: uptoBit使う)
        ruleMiner = "withPruning-cbo"; // pruning付きCellier+cbo版(ppc拡大: uptoBit使う)
        System.out.println("\nfile:" + filename + "\nminsup:" + minsup + "\nminconf:" + minconf);

        Set<Rule> unorderedRules = new ExploreConcepts(filename, minsup, minconf, targetIndex, negObjIndex)
                .run(ruleMiner);
        List<Rule> rules = new ArrayList<Rule>(unorderedRules);
        // リフト順に並び替え
        Collections.sort(rules, new Comparator<Rule>() {
            @Override
            public int compare(Rule r0, Rule r1) {
                return -Float.compare(r0.getStat().getLift(), r1.getStat().getLift());
            }
        });

        for (Rule elm : rules) {
            System.out.println(elm);
        }
    }
}