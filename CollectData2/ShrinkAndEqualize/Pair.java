class Pair<First, Second> {
    private First fst;
    private Second scd;

    public Pair(First f, Second s) {
        fst = f;
        scd = s;
    }

    public First first() {
        return fst;
    }
    public Second second() {
        return scd;
    }
}