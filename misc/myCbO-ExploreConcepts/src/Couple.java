public class Couple implements Comparable<Couple> {
    private Concept concept;
    private int indexAddedToThis; // このconcept生成のために親概念に追加されたattr index

    public Couple(Concept concept, int indx) {
        // TODO 自動生成されたコンストラクター・スタブ
        this.concept = concept;
        this.indexAddedToThis = indx;
    }

    public Concept getConcept() {
        return concept;
    }

    /*
     * public int[] getIntent() { return int_ps; }
     */
    public int getIndexAddedToThis() {
        return indexAddedToThis;
    }

    @Override
    public int compareTo(Couple oth) {
        int mySize = SetOperation.size(concept.getExtent());
        int othSize = SetOperation.size(oth.getConcept().getExtent());
        if (mySize > othSize)
            return 1;
        else if (mySize < othSize)
            return -1;
        else
            return 0;
    }

    @Override
    public String toString() {
        String str = "( " + concept.toString() + ", idx= ";
        str += this.indexAddedToThis + ") ";
        return str;
    }
}