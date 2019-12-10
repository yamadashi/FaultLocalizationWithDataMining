import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

public class ExploreConcepts {

    private int[] context = null; // 文脈
    private int objNum = 0; // オブジェクト数
    private int attrNum = 0; // 属性数
    private int intObjLen = 0; // 属性をbitで管理したときに何個intが必要か
    private int intAttrLen = 0; // オブジェクトをbitで管理したときに何個intが必要か
    int INTSIZE = Constants.INTSIZE;
    int ARCHBIT = INTSIZE - 1; // cbo
    private int[] upto = new int[Constants.INTSIZE]; // 下三角行列状 詳しくはprepareを参照
    private int[][] objHas = null; // 第一要素を属性にもつオブジェクトの配列
    private int[] negObjs = null; // targetIndex属性の反対属性をもつオブジェクトの配列
    private ArrayList<Rule> bestRules = new ArrayList<Rule>();
    private float currentBestVal = 1.0f; // 現在のbest lift value
    private int minsupp; // 最小support
    private float minconf; // 最小confidence
    private int targetIndex; // ルールの後件となる属性のインデックス
    private int negObjIndex; // targetIndex属性の反対属性のインデックス
    private String ruleMiner;
    int[] upto_bit = new int[32];
    static final int BIT = 1;

    public ExploreConcepts(String file, int minsupp, float minconf, int targetIndex, int negObjIndex) {

        this.minsupp = minsupp;
        this.minconf = minconf;
        this.targetIndex = targetIndex;
        this.negObjIndex = negObjIndex;
        readContext(file);
        prepare();
    }

    public Set<Rule> run(String ruleMiner) {
        this.ruleMiner = ruleMiner;
        List<Concept> sol = solve();
        System.out.println("=============================");
        System.out.println("#totalConcepts (sol) : " + sol.size());
        for (Concept c : sol) {
            System.out.println(c.getStat() + "\n\t" + c);
        }
        System.out.println("=============================");
        return calcRules(sol);
    }

    private List<Concept> solve() {

        List<Concept> solution = new ArrayList<>();
        Queue<Couple> exploration = calcInitialExploration();

        while (!exploration.isEmpty()) {
            Couple couple = exploration.poll();
            System.out.println("polled concept: " + couple.getConcept().toString());
            // FILTERの結果の取得
            Pair<Pair<Boolean, Boolean>, Statistics> filterRes = FILTER(couple.getConcept().getExtent());
            boolean KEEP = filterRes.getFirst().getFirst();
            boolean CONTINUE = filterRes.getFirst().getSecond();
            if (couple.getConcept().getIntent()[0] == 1207959552) { // magic number?
                System.out.println("current polled concept: " + couple.getConcept().toString());
                System.out.println("(KEEP, CONTINUE): " + KEEP + ", " + CONTINUE);

            }

            if (KEEP || CONTINUE) {
                if (KEEP) {
                    System.out.println("concept to sol: " + couple.getConcept().toString());
                    solution.add(couple.getConcept());
                }
                if (CONTINUE) {
                    List<Couple> newCouples = doPpcExt(couple);
                    for (Couple newCouple : newCouples) {
                        exploration.add(newCouple);
                    }
                }
            }
        }

        return solution;
    }

    // 拡張でnegObjsSetが不変かチェック. thisIntentは0(targetIndex), 1を含まない.
    private boolean checkKeepNegObjsSet(int[] thisExtent, int[] newExtent) {
        int[] diff = SetOperation.xor(thisExtent, newExtent);
        int[] diffofNegObjs = SetOperation.intersection(diff, negObjs);
        return SetOperation.size(diffofNegObjs) == 0;
    }

    private Queue<Couple> calcInitialExploration() {
        Queue<Couple> initial;
        if (this.ruleMiner.startsWith("cbo")) {
            initial = new PriorityQueue<>(new MyDFSComparator());
        } else if (this.ruleMiner.startsWith("withPruning")) {
            initial = new PriorityQueue<>(new MyDFSComparator());
        } else {
            initial = new PriorityQueue<>();
        }
        /*
         * int INTSIZE = Constants.INTSIZE; int ARCHBIT = INTSIZE - 1; // cbo
         */
        Couple topCouple = getTopCouple();
        System.out.println("topCouple: " + topCouple.toString());
        System.out.println("topCouple.ppcExtMask:");
        CloseByOne.printBit(topCouple.getConcept().getPpcExtMask());
        System.out.println("\n --------");

        // 探索枝刈り ppc拡張mask用bit列. 初期値0
        int[] ppcExtMask = topCouple.getConcept().getPpcExtMask(); // int[intAttrLen];

        System.out.println("doPpcExt for :" + topCouple.toString());
        System.out.println("topCouple.ppcExtMask:");
        CloseByOne.printBit(ppcExtMask);

        List<Couple> childNodes = doPpcExt(topCouple);

        for (Couple elm : childNodes) {
            initial.add(elm);
        }

        System.out.println("initQueue (not ordered) : " + initial.size());
        for (Couple elm : initial) {
            System.out.println(elm.toString());
            // int dec = elm.getConcept().getIntent()[0];
            // System.out.println(elm.getConcept().getExtent()[0] + ", "+
            // elm.getConcept().getIntent()[0]);
        }
        System.out.println("=== end of initQueue =========================");

        return initial;
    }

    // input: Couple (concept, X)
    // output: List<Couple> childNodes. 1回ppc拡張したとき生成される子ノードのリスト
    private List<Couple> doPpcExt(Couple thisCouple) {
        int[] thisExtent = thisCouple.getConcept().getExtent(); // int[intObjLen];
        int[] thisIntent = thisCouple.getConcept().getIntent(); // int[intAttrLen];
        int[] ppcExtMask = thisCouple.getConcept().getPpcExtMask(); // int[intAttrLen];
        int indexAddedToThis = thisCouple.getIndexAddedToThis();

        List<Couple> childNodes = new ArrayList<Couple>();
        ATTR: for (int X = indexAddedToThis + 1; X < attrNum; X++) {

            // X already in topIntent
            int start_int = X / INTSIZE; // Xのbit表現用． X -> (start_int, start_bit)
            int start_bit = ARCHBIT - (X - (start_int * INTSIZE));
            if (((thisIntent[start_int] & (1 << start_bit))) != 0) {
                continue;
            }

            int[] newExtent = new int[intObjLen];
            // X追加後のextentを計算
            boolean empty = true; // X追加後のextentが空集合
            for (int i = 0; i < intObjLen; i++) {
                newExtent[i] = thisExtent[i] & objHas[X][i];
                if (newExtent[i] != 0) {
                    empty = false;
                }
            }
            if (empty)
                continue;

            int[] newIntent = new int[intAttrLen];
            Arrays.fill(newIntent, Constants.BIT_MAX);
            // X追加後のintentの計算
            for (int i = 0; i < intObjLen; i++) {
                for (int j = 0; j < INTSIZE; j++) {
                    // (i*INTSIZE+j)番目のオブジェクトが共通
                    if ((newExtent[i] & (1 << (INTSIZE - j - 1))) != 0) { // (i,j)番目のobj in newExtent
                        for (int k = 0, l = intAttrLen * (i * INTSIZE + j); k < intAttrLen; k++, l++) {
                            newIntent[k] &= context[l]; // (i*INTSIZE+j)番目のオブジェクトが持ってない属性を除く
                        }
                    }
                }
            }

            // ここでppc-ext check. Xより左で intentに違いがあれば捨てる
            for (int i = 0; i < start_int; i++) {
                int diff = newIntent[i] ^ thisIntent[i]; // 差分
                diff &= ~ppcExtMask[i]; // ppcExtMaskでマスキング
                if (diff != 0) { // 違っていたらdiff = 1
                    continue ATTR;
                }
            }
            // start_intのbit列のdiff check
            int diff = (newIntent[start_int] ^ thisIntent[start_int]) & upto_bit[start_bit]; // upto_bitまでの差分
            diff &= ~ppcExtMask[start_int]; // ppcExtMaskでマスキング
            if (diff != 0) {
                continue;
            }

            if (this.ruleMiner.startsWith("cbo")) {
                Concept con = new Concept(newExtent, newIntent, calcStat(newExtent));
                System.out.println("concept gentd: " + con.toString());
                childNodes.add(new Couple(con, X));
            } else if (this.ruleMiner.startsWith("withPruning")) { //
                boolean keepNegObjs = checkKeepNegObjsSet(thisExtent, newExtent);
                System.out.println("empty, keepNegObjs for current X:" + empty + "," + keepNegObjs + ", " + X);

                if (keepNegObjs) {// 探索打ち切り. mask更新
                    ppcExtMask[start_int] |= (1 << start_bit);
                    System.out.println("pruning bykeepNegObjs for X: " + X);
                    CloseByOne.printBit(ppcExtMask);
                    continue ATTR;
                } else {// lift値check
                    int[] targetSet = SetOperation.makeSet(intAttrLen, 0, 1);
                    Concept con = new Concept(newExtent, SetOperation.difference(newIntent, targetSet),
                            calcStat(newExtent), ppcExtMask);
                    childNodes.add(new Couple(con, X));
                    System.out.println("concept gentd: " + con.toString());
                    float newLift = con.getStat().getLift();
                    if (this.currentBestVal <= newLift) {
                        this.currentBestVal = newLift;
                        this.bestRules.add(calcRule(con));
                        continue ATTR;
                    } else {
                        continue ATTR;
                    }
                    // initial.add(new Couple(con, X));
                }

            }
        }

        return childNodes;
    }

    private Couple getTopCouple() {
        int[] topExtent = new int[intObjLen];
        int[] topIntent = new int[intAttrLen];

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
        // 探索枝刈り ppc拡張mask用bit列. 初期値0. ただし，targets 0,1は1にset.
        int[] ppcExtMask = SetOperation.makeSet(intAttrLen, 0, 1);

        // ppc拡張のstart値 = 2 (0,1はクラス用)
        return new Couple(new Concept(topExtent, topIntent, calcStat(topExtent), ppcExtMask), 1);
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
                if (SetOperation.size(removed) == 0)
                    continue;
                rules.add(new Rule(removed, targetIndex, calcStat(calcExtent.apply(removed))));
            } else {
                rules.add(new Rule(c.getIntent(), targetIndex, c.getStat()));
            }
        }
        return rules;
    }

    private Rule calcRule(Concept con) {

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

        int[] intent = con.getIntent();
        Rule rule = null;
        if (flagIsSet.test(intent, targetIndex)) { // intentにtargetIndex(ルール結論部の属性)が含まれる
            int[] removed = flagUnset.apply(intent, targetIndex);
            if (SetOperation.size(removed) == 0)
                removed = new int[intAttrLen];
            rule = new Rule(removed, targetIndex, calcStat(calcExtent.apply(removed)));
        } else {
            rule = new Rule(con.getIntent(), targetIndex, con.getStat());
        }
        return rule;
    }

    private Pair<Pair<Boolean, Boolean>, Statistics> FILTER(int[] ext) {

        Statistics stat = calcStat(ext);
        if (ext[0] == 1342177280) {
            System.out.println("ext[0]");
            CloseByOne.printBit(ext[0]);
            System.out.println("(supp, conf): " + stat.getSupp() + ", " + stat.getConf());
        }
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
        System.out.println("intObjLen:" + intObjLen + "\n" + "intAttrLen:" + intAttrLen);

        context = new int[objNum * intAttrLen];
        for (int i = 0; i < objNum; i++) {
            int[] obj = buff.get(i);
            for (int index : obj) {
                context[i * intAttrLen + index / INTSIZE] |= 1 << (INTSIZE - (index % INTSIZE) - 1);
            }
        }
        System.out.println("context (in raedCxt):");
        CloseByOne.printBit(context);
    }

    private void prepare() {

        int INTSIZE = Constants.INTSIZE;
        int ARCHBIT = INTSIZE - 1;
        for (int i = 0; i <= ARCHBIT; i++) {
            for (int j = ARCHBIT; j > i; j--) {
                upto_bit[i] |= (BIT << j);
            }
        }

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
        negObjs = new int[intObjLen];
        for (int i = 0; i < intObjLen; i++) {
            negObjs[i] = objHas[this.negObjIndex][i];
        }
    }

    private Statistics calcStat(int[] ext) {

        // targetIndexを持つobjectsとextentの共通集合( ext_T ∩ ext_s )
        int[] tmp = ext.clone();
        for (int i = 0; i < intObjLen; i++) {
            tmp[i] &= objHas[targetIndex][i];
        }

        int sup = SetOperation.size(tmp); // || ext_s ∩ ext_T ||
        float conf = (float) sup / SetOperation.size(ext);
        float lift = conf / ((float) SetOperation.size(objHas[targetIndex]) / objNum);

        return new Statistics(sup, conf, lift);
    }

    public static String toStringBit(int bit) {
        return String.format("%32s", Integer.toBinaryString(bit)).replaceAll(" ", "0");
    }
}