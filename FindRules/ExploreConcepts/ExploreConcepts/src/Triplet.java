import java.util.List;

public class Triplet {
    Mapping map;
    int[] intent; // 不要
    List<Mapping> increments;

    public Triplet(Mapping map, int[] intent, List<Mapping> increments) {
        this.map = map;
        this.intent = intent;
        this.increments = increments;
    }

    public Mapping getMap() {
        return map;
    }

    public int[] getIntent() {
        return intent;
    }

    public List<Mapping> getIncr() {
        return increments;
    }

    @Override
    public String toString() {
        String str = "( "+ map +", "+Concept.toString(intent)+", { ";
        for (Mapping m : increments) {
            str += m + " ";
        }
        str += "} )";
        return str;
    }
}