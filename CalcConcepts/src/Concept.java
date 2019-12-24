// 概念
public class Concept implements Cloneable {
    private int id;
    protected int[] extent; // 外延
    protected int[] intent; // 内包
    protected int[] ppcExtMask;

    private static int nextID = 0;
    protected static final int INTSIZE = Integer.SIZE;
    protected static final int BIT = 1;

    public Concept(int[] ex, int[] in, int[] ppcExtMask) {
        this.extent = ex;
        this.intent = in;
        this.ppcExtMask = ppcExtMask;
        this.id = nextID++;
    }

    public int[] getExtent() {
        return extent;
    }

    public int[] getIntent() {
        return intent;
    }

    @Override
    public String toString() {
        String str = id + ": { ";
        for (int i = 0; i < extent.length; i++) {
            for (int j = 0; j < INTSIZE; j++) {
                if ((extent[i] & BIT << (INTSIZE - 1 - j)) != 0) {
                    str += ((i * INTSIZE + j) + " ");
                }
            }
        }
        str += "} / { ";
        for (int i = 0; i < intent.length; i++) {
            for (int j = 0; j < INTSIZE; j++) {
                if ((intent[i] & BIT << (INTSIZE - 1 - j)) != 0) {
                    str += ((i * INTSIZE + j) + " ");
                }
            }
        }
        str += "}";
        return str;
    }

    @Override
    public Concept clone() {
        return new Concept(extent.clone(), intent.clone(), ppcExtMask.clone());
    }

    public boolean checkPPC(int current, Concept prev, int[] upto, int[] negObjs) {
        for (int i = 0; i < current / INTSIZE; i++) {
            int diff = intent[i] ^ prev.getIntent()[i];
            diff &= ~ppcExtMask[i];
            if (diff != 0) {
                return false;
            }
        }
        int diff = intent[current / INTSIZE] ^ prev.getIntent()[current / INTSIZE];
        diff &= ~ppcExtMask[current / INTSIZE];
        if ((diff & upto[current % INTSIZE]) != 0) {
            return false;
        }
        return true;
    }
}