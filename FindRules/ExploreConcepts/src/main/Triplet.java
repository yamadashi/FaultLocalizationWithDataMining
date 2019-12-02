package main;

import java.util.List;
import java.lang.Comparable;

public class Triplet implements Comparable<Triplet> {
    Mapping map;
    Concept parent; // 不要
    List<Mapping> increments;

    public Triplet(Mapping map, Concept parent, List<Mapping> increments) {
        this.map = map;
        this.parent = parent;
        this.increments = increments;
    }

    public Mapping getMap() {
        return map;
    }

    public Concept getParent() {
        return parent;
    }

    public List<Mapping> getIncr() {
        return increments;
    }

    @Override
    public int compareTo(Triplet oth) {
        int mySize = ExploreConcepts.bitCount(map.getChild().getExtent());
        int othSize = ExploreConcepts.bitCount(oth.map.getChild().getExtent());
        if (mySize > othSize)
            return 1;
        else if (mySize < othSize)
            return -1;
        else
            return 0;
    }

    @Override
    public String toString() {
        String str = "( " + map + ", " + parent + ", { ";
        for (Mapping m : increments) {
            str += m + " ";
        }
        str += "} )";
        return str;
    }
}