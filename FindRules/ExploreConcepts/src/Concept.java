
// 概念
public class Concept {
    private int[] extent; // 外延
    private int[] intent; // 内包

    private static final int BIT = 1;
    private static final int INTSIZE = Integer.SIZE;

    public Concept(int[] ex, int[] in) {
        this.extent = ex;
        this.intent = in;
    }

    public int[] getExtent() {
        return extent;
    }

    public int[] getIntent() {
        return intent;
    }

    public void print() {
        String str = "{ ";
        for (int i = 0; i < extent.length; i++) {
            for (int j = INTSIZE - 1; j >= 0; j--) {
                if ((extent[i] & BIT << j) != 0) {
                    str += ((INTSIZE - j - 1 + i * (INTSIZE)) + " ");
                }
            }
        }
        str += "} / { ";
        for (int i = 0; i < intent.length; i++) {
            for (int j = INTSIZE - 1; j >= 0; j--) {
                if ((intent[i] & BIT << j) != 0) {
                    str += ((INTSIZE - j - 1 + i * (INTSIZE)) + " ");
                }
            }
        }
        str += "}";
        System.out.println(str);
    }
}