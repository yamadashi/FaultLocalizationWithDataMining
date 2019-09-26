
// 概念
public class Concept {

    private int[] extent; // 外延
    private int[] intent; // 内包
    // 統計量 すべての概念について必要ではないのでnull許容
    // 現状はFILTER関数実行時に求めているが、TripletComparatorで利用する場合はコンストラクタで初期化するように変更する
    private Statistics stat = null;

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

    public Statistics getStat() {
        return stat;
    }

    public void setStat(Statistics stat_) {
        stat = stat_;
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

    static class Statistics {
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
}