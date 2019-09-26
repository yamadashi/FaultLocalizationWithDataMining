public class Mapping {
    private Concept child;
    private int diff; // 差分属性の番号

    public Mapping(Concept child, int diff) {
        this.child = child;
        this.diff = diff;
    }

    public Concept getChild() {
        return child;
    }

    public int getDiff() {
        return diff;
    }

    @Override
    public String toString() {
        return "("+child+")→"+diff;
    }
}