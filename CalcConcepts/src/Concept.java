// 概念
public class Concept implements Cloneable {
    private int id;
    protected int[] extent; // 外延
    protected int[] intent; // 内包

    private static int nextID = 0;
    protected static final int INTSIZE = Integer.SIZE;
    protected static final int BIT = 1;

    public Concept(int[] ex, int[] in) {
        this.extent = ex;
        this.intent = in;
        this.id = nextID++;
    }

    public int getID() {
        return id;
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
        return new Concept(extent.clone(), intent.clone());
    }

    public boolean checkPPC(int current, Concept prev, int[] upto, int[] negObjs) {
        for (int i = 0; i < current / INTSIZE; i++) {
            if ((intent[i] ^ prev.getIntent()[i]) != 0) {
                return false;
            }
        }
        if (((intent[current / INTSIZE] ^ prev.getIntent()[current / INTSIZE]) & upto[current % INTSIZE]) != 0) {
            return false;
        }
        return true;
    }
}