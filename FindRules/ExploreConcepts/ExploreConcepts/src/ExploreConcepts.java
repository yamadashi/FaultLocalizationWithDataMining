import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.function.*;

public class ExploreConcepts {

    private int[] context = null; // 文脈
    private int objNum = 0; // オブジェクト数
    private int attrNum = 0; // 属性数
    private int intObjLen = 0; // 属性をbitで管理したときに何個intが必要か
    private int intAttrLen = 0; // オブジェクトをbitで管理したときに何個intが必要か
    private int[] upto = new int[Constants.INTSIZE]; // 下三角行列状 詳しくはprepareを参照
    private int[][] objHas = null; // 第一要素を属性にもつオブジェクトの配列

    private int minsupp; // 最小support
    private float minconf; // 最小confidence
    private int targetIndex; // ルールの後件となる属性のインデックス

    public ExploreConcepts(String file, int minsupp, float minconf, int targetIndex) {

        this.minsupp = minsupp;
        this.minconf = minconf;
        this.targetIndex = targetIndex;

        readContext(file);
        prepare();
    }

    public Set<Rule> run() {
        List<Concept> sol = solve();
        System.out.println("=============================");
        for (Concept c : sol) {
            System.out.println(c.getStat() + "\n\t" + c);
        }
        System.out.println("=============================");
        return calcRules(sol);
    }

    private List<Concept> solve() {
        List<Concept> solution = new ArrayList<>();
        Queue<Triplet> exploration = calcInitialExploration();

        // 局所的なヘルパー関数群
        BinaryOperator<int[]> union = (s0, s1) -> {
            int[] rtn = s0.clone();
            for (int i = 0; i < rtn.length; i++) {
                int tmp = s1[i];
                rtn[i] |= tmp;
            }
            return rtn;
        };
        BinaryOperator<int[]> intersection = (s0, s1) -> {
            int[] rtn = s0.clone();
            for (int i = 0; i < rtn.length; i++) {
                rtn[i] &= s1[i];
            }
            return rtn;
        };

        while (!exploration.isEmpty()) {
            Triplet triplet = exploration.poll();

            // FILTERの結果の取得
            Pair<Pair<Boolean, Boolean>, Statistics> filterRes = FILTER(triplet.getMap().getExtent());
            boolean KEEP = filterRes.getFirst().getFirst();
            boolean CONTINUE = filterRes.getFirst().getSecond();
            Statistics stat = filterRes.getSecond();

            if (KEEP || CONTINUE) {
                int[] int_s = union.apply(triplet.getIntent(), triplet.getMap().getX());

                if (KEEP) {
                    solution.add(new Concept(triplet.getMap().getExtent(), int_s, stat));
                }
                if (CONTINUE) {
                    List<Mapping> incr_s = calcIncrements(triplet.getMap(), triplet.getIncr(), intersection);
                    for (Mapping map : incr_s) {
                        exploration.add(new Triplet(map, int_s, incr_s));
                    }
                }
            }
        }

        return solution;
    }

    private List<Mapping> calcIncrements(Mapping map_ps, List<Mapping> incr_ps, BinaryOperator<int[]> intersection) {
        List<Mapping> incr_s = new ArrayList<>();
        int[] ext_s = map_ps.getExtent();
        for (Mapping map : incr_ps) {
            if (map == map_ps)
                continue;
            int[] c = intersection.apply(ext_s, map.getExtent());
            if (bitCount(c) >= minsupp) {
                incr_s.add(new Mapping(c, map.getX()));
            }
        }
        return incr_s;
    }

    private Queue<Triplet> calcInitialExploration() {
        Queue<Triplet> initial = new PriorityQueue<>();
        List<Mapping> incr = new ArrayList<>();

        int INTSIZE = Constants.INTSIZE;
        int[] topExtent = new int[intObjLen];
        int[] topIntent = new int[intAttrLen];

        // 局所的なヘルパー関数
        BinaryOperator<int[]> difference = (s0, s1) -> {
            int[] rtn = s0.clone();
            for (int i = 0; i < rtn.length; i++) {
                rtn[i] &= ~s1[i];
            }
            return rtn;
        };

        // 全オブジェクトに対応するbitを立てる
        Arrays.fill(topExtent, 0);
        for (int i = 0; i < intObjLen - 1; i++) {
            topExtent[i] = Constants.BIT_MAX;
        }
        for (int i = 0; i < objNum % INTSIZE; i++) {
            topExtent[intObjLen - 1] |= (1 << (INTSIZE - i - 1));
        }
        // 全オブジェクトに共通の属性bitを立てる
        Arrays.fill(topIntent, Constants.BIT_MAX);
        for (int i = 0; i < objNum; i++) {
            for (int j = 0; j < intAttrLen; j++)
                topIntent[j] &= context[intAttrLen * i + j];
        }
        // incrの計算
        for (int X = 0; X < attrNum; X++) {
            int[] newExtent = new int[intObjLen];
            // X追加後のextentを計算
            boolean empty = true; // X追加後のextentが空集合
            for (int i = 0; i < intObjLen; i++) {
                newExtent[i] = topExtent[i] & objHas[X][i];
                if (newExtent[i] != 0) {
                    empty = false;
                }
            }

            if (!empty) {
                int[] newIntent = new int[intAttrLen];
                Arrays.fill(newIntent, Constants.BIT_MAX);
                // X追加後のintentの計算
                for (int i = 0; i < intObjLen; i++) {
                    for (int j = 0; j < INTSIZE; j++) {
                        // (iINTSIZE+j)番目のオブジェクトが共通
                        if ((newExtent[i] & (1 << (INTSIZE - j - 1))) != 0) {
                            for (int k = 0, l = intAttrLen * (i * INTSIZE + j); k < intAttrLen; k++, l++) {
                                newIntent[k] &= context[l]; // (iINTSIZE+j)番目のオブジェクトが持ってない属性を除く
                            }
                        }
                    }
                }
                int[] diff = difference.apply(newIntent, topIntent);
                incr.add(new Mapping(newExtent, diff));
            }
        }

        for (Mapping map : incr) {
            initial.add(new Triplet(map, topIntent, incr));
        }

        return initial;
    }

    private Set<Rule> calcRules(List<Concept> sol) {
        Set<Rule> rules = new HashSet<Rule>();

        // 局所的なヘルパー関数群
        BiPredicate<int[], Integer> flagIsSet = (data, index) -> {
            int INTSIZE = Constants.INTSIZE;
            return (data[index / INTSIZE] & (1 << (index % INTSIZE - 1))) != 0;
        };
        BiFunction<int[], Integer, int[]> flagUnset = (origin, index) -> {
            int[] rtn = origin.clone();
            int INTSIZE = Constants.INTSIZE;
            rtn[index / INTSIZE] &= ~(1 << (index % Constants.INTSIZE - 1));
            return rtn;
        };
        UnaryOperator<int[]> calcExtent = intent -> {
            int[] ext = new int[intObjLen];
            Arrays.fill(ext, Constants.BIT_MAX);
            int INTSIZE = Constants.INTSIZE;
            for (int i = 0; i < attrNum; i++) {
                if ((intent[i / INTSIZE] & (1 << (INTSIZE - 1 - (i % INTSIZE)))) == 0)
                    continue;
                for (int j = 0; j < intObjLen; j++)
                    ext[j] &= objHas[i][j];
            }
            return ext;
        };

        for (Concept c : sol) {
            int[] intent = c.getIntent();
            if (flagIsSet.test(intent, targetIndex)) {
                int[] removed = flagUnset.apply(intent, targetIndex);
                if (bitCount(removed) == 0)
                    continue;
                rules.add(new Rule(removed, targetIndex, calcStat(calcExtent.apply(removed))));
            } else {
                rules.add(new Rule(c.getIntent(), targetIndex, c.getStat()));
            }
        }
        return rules;
    }

    private Pair<Pair<Boolean, Boolean>, Statistics> FILTER(int[] ext) {

        Statistics stat = calcStat(ext);

        boolean KEEP = stat.getSupp() >= minsupp && stat.getConf() >= minconf;
        boolean CONTINUE = stat.getSupp() >= minsupp;

        Pair<Boolean, Boolean> flags = new Pair<>(KEEP, CONTINUE);

        return new Pair<>(flags, stat);
    }

    private void readContext(String file) {

        List<int[]> buff = new ArrayList<int[]>();
        int maxAttrIndex = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(",");
                int[] attrIndices = new int[splitLine.length];
                for (int i = 0; i < attrIndices.length; i++) {
                    int attrIndice = Integer.parseInt(splitLine[i]);
                    attrIndices[i] = attrIndice;
                    if (maxAttrIndex < attrIndice) {
                        maxAttrIndex = attrIndice;
                    }
                }
                buff.add(attrIndices);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("error:" + e);
        }

        int INTSIZE = Constants.INTSIZE;

        objNum = buff.size();
        attrNum = maxAttrIndex + 1; // indexは0からなので
        System.out.println("attr:" + attrNum + "\n" + "obj:" + objNum);
        intObjLen = objNum / INTSIZE + 1;
        intAttrLen = attrNum / INTSIZE + 1;

        context = new int[objNum * intAttrLen];
        for (int i = 0; i < objNum; i++) {
            int[] obj = buff.get(i);
            for (int index : obj) {
                context[i * intAttrLen + index / INTSIZE] |= 1 << (INTSIZE - (index % INTSIZE) - 1);
            }
        }
    }

    private void prepare() {

        int INTSIZE = Constants.INTSIZE;

        for (int i = 0; i < INTSIZE; i++) {
            for (int j = 0; j < i; j++) {
                upto[i] |= (1 << (INTSIZE - 1 - j));
            }
        }
        // uptoは以下のような行列になる
        // 0 0 0 .. 0 0
        // 1 0 0 .. 0 0
        // 1 1 0 .. 0 0
        // 1 1 1 .. 0 0
        // : :
        // 1 1 1 .. 1 0

        objHas = new int[intAttrLen * INTSIZE][intObjLen];

        for (int i = 0; i < intAttrLen; i++) {
            for (int j = 0; j < INTSIZE; j++) {
                int mask = (1 << j);
                for (int x = 0, y = i; x < objNum; x++, y += intAttrLen) {
                    if ((context[y] & mask) != 0) {
                        int attr = i * INTSIZE + (INTSIZE - j - 1);
                        objHas[attr][x / INTSIZE] |= 1 << (INTSIZE - (x % INTSIZE) - 1);
                    }
                }
            }
        }
    }

    private Statistics calcStat(int[] ext) {

        // targetIndexを持つobjectsとextentの共通集合( ext_T ∩ ext_s )
        int[] tmp = ext.clone();
        for (int i = 0; i < intObjLen; i++) {
            tmp[i] &= objHas[targetIndex][i];
        }

        int sup = bitCount(tmp); // || ext_s ∩ ext_T ||
        float conf = (float) sup / bitCount(ext);
        float lift = conf / ((float) bitCount(objHas[targetIndex]) / objNum);

        return new Statistics(sup, conf, lift);
    }

    // int[] の立っているbit数を数える
    public static int bitCount(int[] arr) {
        int rtn = 0;
        for (int e : arr) {
            rtn += Integer.bitCount(e);
        }
        return rtn;
    }

    public static String toStringBit(int bit) {
        return String.format("%32s", Integer.toBinaryString(bit)).replaceAll(" ", "0");
    }
}