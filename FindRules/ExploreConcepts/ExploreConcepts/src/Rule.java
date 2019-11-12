public class Rule {
    private Statistics stat;
    private int[] premise;
    private int conclusion;

    public Rule(int[] premise, int conclusion, Statistics stat) {
        this.premise = premise;
        this.conclusion = conclusion;
        this.stat = stat;
    }

    public Statistics getStat() {
        return stat;
    }

    public int[] getPremise() {
        return premise;
    }

    public void setPremise(int[] premise) {
        this.premise = premise;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(premise[0]);
    }

    @Override
    public boolean equals(Object o) {
        Rule otherRule = (Rule) o;
        boolean rtn = true;
        for (int i = 0; i < premise.length; i++) {
            if (premise[i] != otherRule.premise[i])
                rtn = false;
        }
        return rtn;
    }

    @Override
    public String toString() {
        return stat + "\n\t" + Concept.toString(premise) + " -> " + conclusion;
    }
}