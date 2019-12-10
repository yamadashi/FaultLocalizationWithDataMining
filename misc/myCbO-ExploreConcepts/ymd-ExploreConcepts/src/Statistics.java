public class Statistics {
    private int support;
    private float confidence;
    private float lift;

    public Statistics(int supp, float conf, float lif) {
        support = supp;
        confidence = conf;
        lift = lif;
    }

    public int getSupp() {
        return support;
    }

    public float getConf() {
        return confidence;
    }

    public float getLift() {
        return lift;
    }

    @Override
    public String toString() {
        return "sup:" + support + " conf:" + confidence + " lift:" + lift;
    }
}