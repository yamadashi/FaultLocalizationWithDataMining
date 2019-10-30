import java.util.List;
import java.lang.Comparable;

public class Triplet implements Comparable<Triplet> {
    private Mapping map;
    private int[] int_ps;
    private List<Mapping> increments;

    public Triplet(Mapping map, int[] int_ps, List<Mapping> increments) {
        this.map = map;
        this.int_ps = int_ps;
        this.increments = increments;
    }

    public Mapping getMap() {
        return map;
    }

    public int[] getIntent() {
        return int_ps;
    }

    public List<Mapping> getIncr() {
        return increments;
    }

    @Override
    public int compareTo(Triplet oth) {
        int mySize = ExploreConcepts.bitCount(map.getExtent());
        int othSize = ExploreConcepts.bitCount(oth.getMap().getExtent());
        if (mySize > othSize)
            return 1;
        else if (mySize < othSize)
            return -1;
        else
            return 0;
    }

    @Override
    public String toString() {
        String str = "( " + map + ", " + Concept.toString(int_ps) + ", { ";
        for (Mapping m : increments) {
            str += m + " ";
        }
        str += "} )";
        return str;
    }
}