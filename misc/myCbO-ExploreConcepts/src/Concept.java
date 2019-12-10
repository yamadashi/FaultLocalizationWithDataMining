// 概念
public class Concept {

    private int[] extent; // 外延
    private int[] intent; // 内包
    private Statistics stat; // 統計量
    private int[] ppcExtMask; // 枝刈り用bit列(intAtrLen)


    public Concept(int[] ex, int[] in, Statistics stat) {
        this.extent = ex;
        this.intent = in;
        this.stat = stat;
    }

    public Concept(int[] newExtent, int[] newIntent, Statistics calcStat, int[] ppcExtMask) {
        this.extent = newExtent;
        this.intent = newIntent;
        this.stat = calcStat;
        this.ppcExtMask = ppcExtMask;
	}

	public int[] getExtent() {
        return extent;
    }

    public int[] getIntent() {
        return intent;
    }

    public Statistics getStat() {
        return stat;
    }

    public int[] getPpcExtMask() {
        return ppcExtMask;
    }

    public void setStat(Statistics stat) {
        this.stat = stat;
    }

    public static String toString(int[] elm) {

        int INTSIZE = Constants.INTSIZE;

        String str = "{ ";
        for (int i = 0; i < elm.length; i++) {
            for (int j = INTSIZE - 1; j >= 0; j--) {
                if ((elm[i] & 1 << j) != 0) {
                    str += ((INTSIZE - j - 1 + i * (INTSIZE)) + " ");
                }
            }
        }
        str += "}";
        return str;
    }

    @Override
    public String toString() {
        return toString(extent) + "/" + toString(intent);
    }
}