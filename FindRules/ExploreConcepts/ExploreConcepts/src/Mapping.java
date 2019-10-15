public class Mapping {
    private int[] ext_s;
    private int X; // 差分属性の番号

    public Mapping(int[] ext_s, int X) {
        this.ext_s = ext_s;
        this.X = X;
    }

    public int[] getExtent() {
        return ext_s;
    }

    public int getX() {
        return X;
    }

    @Override
    public String toString() {
        return "(" + Concept.toString(ext_s) + ")→" + X;
    }
}