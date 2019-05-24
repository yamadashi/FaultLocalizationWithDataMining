// 概念
public class Concept {
    private int id;
    private int[] extent; // 外延
    private int[] intent; // 内包

    private static int nextID = 0;
    private static int intObjLen;
    private static int intAttrLen;
    private static final int INTSIZE = Integer.SIZE;
    private static final int BIT = 1;

    public Concept(int[] ex, int[] in) {
        this.extent = ex;
        this.intent = in;
        this.id = nextID++;
    }

    public int[] getExtent() {
        return extent;
    }

    public int[] getIntent() {
        return intent;
    }

    public static void setProperties(int intObjLen_, int intAttrLen_) {
        intObjLen = intObjLen_;
        intAttrLen = intAttrLen_;
    }

    public static Concept createVoidConcept() {
        Concept concept = new Concept(new int[intObjLen], new int[intAttrLen]);
        return concept;
    }

    public static void printConcept(Concept concept) {
        String str = concept.id + ": { ";
        for (int i = 0; i < intObjLen; i++) {
            for (int j = 0; j < INTSIZE; j++) {
                if ((concept.extent[i] & BIT << j) != 0) {
                    str += ((i * INTSIZE + j) + " ");
                }
            }
        }
        str += "} / { ";
        for (int i = 0; i < intAttrLen; i++) {
            for (int j = INTSIZE - 1; j >= 0; j--) {
                if ((concept.intent[i] & BIT << j) != 0) {
                    str += ((INTSIZE - j - 1 + i * (INTSIZE)) + " ");
                }
            }
        }
        str += "}";
        System.out.println(str);
    }
}