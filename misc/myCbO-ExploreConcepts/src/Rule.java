public class Rule {
    private Statistics stat;
    private int[] premise; // nullの場合あり  true -> 0 (targetIndex
    private int conclusion;

    public Rule(int[] premise, int conclusion, Statistics stat) {
        this.premise = premise;
        this.conclusion = conclusion;
        this.stat = stat;
    }

    public Statistics getStat() {
        return stat;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(premise[0]);
    }

    @Override
    public boolean equals(Object o) {
        Rule otherRule = (Rule)o;
        boolean rtn = true;
        for (int i = 0; i < premise.length; i++) {
            if (premise[i] != otherRule.premise[i]) rtn = false;
        }
        return rtn;
    }

    @Override
    public String toString() {
        return stat + "\n\t" + Concept.toString(premise) + " -> " + conclusion;
    }
}