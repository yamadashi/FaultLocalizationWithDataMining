// 概念
public class Concept {
    private int id;
    private int[] extent; // 外延
    private int[] intent; // 内包

    private static int nextID = 0;
    private static int intObjLen;
    private static int intAttrLen;
    private static int INTSIZE = Integer.SIZE;

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
        for (int j = 0; j < intObjLen; ++j) {
            for (int i = 0; i < INTSIZE; ++i) {
                if ((concept.extent[j] & 1 << i) != 0) {
                    str += ((i + j * (INTSIZE - 1)) + " "); // ?
                }
            }
        }
        str += "} / { ";
        for (int j = 0; j < intAttrLen; ++j) {
            for (int i = 31; i >= 0; --i) {
                if ((concept.intent[j] & 1 << i) != 0) {
                    str += ((31 - i + j * (INTSIZE - 1)) + " "); // ?
                }
            }
        }
        str += "}";
        System.out.println(str);
    }
}