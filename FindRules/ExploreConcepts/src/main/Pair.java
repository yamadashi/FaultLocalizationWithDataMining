package main;

class Pair<First, Second> {
    private First f;
    private Second s;

    public Pair(First f, Second s) {
        this.f = f;
        this.s = s;
    }

    public First getFirst() {
        return f;
    }

    public Second getSecond() {
        return s;
    }

    public void setFirst(First f) {
        this.f = f;
    }

    public void setSecond(Second s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return "(" + f + ", " + s + ")";
    }

}