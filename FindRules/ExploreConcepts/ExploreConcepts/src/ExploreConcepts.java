
// solutionの内包からルールの前件として不要なもの(共通な属性集合、ターゲット)を除く
// contextから無駄を省くほうが探索時間も減るので効率的？

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

public class ExploreConcepts {

    private List<Concept> solution;
    private Queue<Triplet> exploration;

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

        solution = new ArrayList<>();
        exploration = new PriorityQueue<Triplet>();

        readContext(file);
        prepare();
    }

    public Set<Rule> run() {
        List<Concept> sol = solve();
        System.out.println("=============================");
        for (Concept c : sol) {
            System.out.println(c.getStat()+"\n\t"+c);
        }
        System.out.println("=============================");
        return makeRules(sol);
    }

    private List<Concept> solve() {

        // 初期状態
        Concept top = computeClosure(null, null);
        for (Triplet tri : getChildren(top, 0)) {
            exploration.add(tri);
        }

        while (!exploration.isEmpty()) {
            Triplet triplet = exploration.poll();
            Concept s = triplet.getMap().getChild();

            // FILTERの結果の取得
            Pair<Boolean, Boolean> flags = FILTER(s);
            boolean KEEP = flags.getFirst();
            boolean CONTINUE = flags.getSecond();

            if (KEEP || CONTINUE) {

                if (KEEP) {
                    solution.add(s);
                }
                if (CONTINUE) {
                    int diffAttr = triplet.getMap().getDiff();
                    List<Triplet> children = getChildren(s, diffAttr + 1);
                    if (children == null)
                        continue;

                    for (Triplet tri : children) {
                        exploration.add(tri);
                    }
                }
            }
        }

        return solution;
    }

    private Set<Rule> makeRules(List<Concept> sol) {
        Set<Rule> rules = new HashSet<Rule>();
        for (Concept c : sol) {
            // もし解のコンセプトの内包がターゲットの属性を持っていたら
            if (flagIsSet(c.getIntent(), targetIndex)) {
                List<Concept> path = getPathFromTop(c);
                int[] removed = unsetFlag(c.getIntent(), targetIndex);
                for (Concept predecessor : path) {
                    System.out.println(predecessor);
                    if (!contain(predecessor.getIntent(), removed)) continue;
                    System.out.println(Concept.toString(removed));
                    Rule r = new Rule(removed, targetIndex, predecessor.getStat());
                    System.out.println(r);
                    System.out.println(r.hashCode());
                    boolean f = rules.add(r);
                    System.out.println(f);
                    break;
                }
            }
            else {
                System.out.println(c);
                Rule r = new Rule(c.getIntent(), targetIndex, c.getStat());
                System.out.println(r);
                System.out.println(r.hashCode());
                boolean f = rules.add(r);
                System.out.println(f);
            }
            System.out.println("------");
        }
        return rules;
    }

    private Pair<Boolean, Boolean> FILTER(Concept s) {

        Statistics stat = makeStat(s.getExtent());

        boolean KEEP = stat.getSupp() >= minsupp && stat.getConf() >= minconf;
        boolean CONTINUE = stat.getSupp() >= minsupp;

        Pair<Boolean, Boolean> flags = new Pair<>(KEEP, CONTINUE);
        s.setStat(stat);

        return flags;
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
        // :          :
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

    private Concept computeClosure(Concept prev, int[] attrExtent) {

        int[] extent = new int[intObjLen];
        int[] intent = new int[intAttrLen];
        Statistics stat = null;

        Arrays.fill(extent, 0);
        Arrays.fill(intent, Constants.BIT_MAX);

        int INTSIZE = Constants.INTSIZE;
        if (attrExtent == null) { // 初期状態としてルート(トップ)の概念を返す
            //全オブジェクトに対応するbitを立たせる
            for (int i = 0; i < intObjLen - 1; ++i) {
                extent[i] = Constants.BIT_MAX;
            }
            for (int i = 0; i < objNum % INTSIZE; i++) {
                extent[intObjLen - 1] |= (1 << (INTSIZE - i - 1));
            }

            for (int j = 0; j < objNum; ++j) {
                for (int i = 0; i < intAttrLen; ++i)
                    intent[i] &= context[intAttrLen * j + i];
            }
            stat = makeStat(extent);
        } else {
            for (int k = 0; k < intObjLen; ++k) {
                extent[k] = prev.getExtent()[k] & attrExtent[k]; // 共通のオブジェクト
                if (extent[k] != 0) { // 共通のオブジェクトがあった場合
                    for (int l = 0; l < INTSIZE; ++l) {
                        if ((extent[k] & (1 << (INTSIZE - l - 1))) != 0) { // (k*INTSIZE+l)番目のオブジェクトが共通
                            for (int i = 0, j = intAttrLen * (k * INTSIZE + l); i < intAttrLen; ++i, ++j) {
                                intent[i] &= context[j]; // (k*INTSIZE+l)番目のオブジェクトが持ってない属性を除く
                            }
                        }
                    }
                } else
                    return null;
            }
        }
        return new Concept(extent, intent, prev, stat);
    }

    private List<Triplet> getChildren(Concept par, int attrOffset) {
        List<Triplet> children = new ArrayList<Triplet>();
        List<Mapping> increments = new ArrayList<Mapping>();
        if (attrOffset >= attrNum)
            return null;

        System.out.println("par:"+par);

        int INTSIZE = Constants.INTSIZE;
        ATTR: for (int i = attrOffset; i < attrNum; i++) {
            // 属性をすでに内包として持っている場合
            if ((par.getIntent()[i / INTSIZE] & (1 << (INTSIZE - (i % INTSIZE) - 1))) != 0) {
                continue;
            }
            Concept child = computeClosure(par, objHas[i]);
            if (child == null)
                continue;

            // 親conceptと内包が一致していない場合
            for (int j = 0; j < i / INTSIZE; j++) {
                if ((child.getIntent()[j] ^ par.getIntent()[j]) != 0) {
                    continue ATTR;
                }
            }
            if (((child.getIntent()[i / INTSIZE] ^ par.getIntent()[i / INTSIZE])
                    & upto[i % INTSIZE]) != 0) {
                continue;
            }

            System.out.println("  diff:" + i + " child:"+ child);
            increments.add(new Mapping(child, i));
        }

        for (Mapping mapping : increments) {
            children.add(new Triplet(mapping, par, increments));
        }

        if (children.size() == 0)
            return null;
        return children;
    }

    private boolean contain(int[] sup, int[] sub) {
        boolean rtn = true;
        for (int i = 0; i < sup.length; i++) {
            if (sup[i] != (sup[i] | sub[i])) rtn = false;
        }
        return rtn;
    }

    private boolean flagIsSet(int[] data, int index) {
        int INTSIZE = Constants.INTSIZE;
        return (data[index / INTSIZE] & setFlag(0, index % INTSIZE)) != 0;
    }

    private int[] unsetFlag(int[] origin, int index) {
        int[] rtn = origin.clone();
        int arrIndex = index / Constants.INTSIZE; 
        rtn[arrIndex] &= ~setFlag(0, index % Constants.INTSIZE);
        return rtn;
    }

    // このメソッドを使ってリファクタリングの余地あり
    // 指定位置のビットを立てたデータを返す
    private int setFlag(int origin, int index) {
        return origin | (1 << (index % Constants.INTSIZE - 1));
    }

    // 下からたどったほうがいいかもしれない
    // 該当コンセプトまでの道のりを返す
    private List<Concept> getPathFromTop(Concept c) {
        List<Concept> path = new ArrayList<Concept>();
        while(c != null) {
            path.add(0, c); // 先頭に追加
            c = c.getParent();
        }
        return path;
    }
    private Statistics makeStat(int[] ext) {

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
    static int bitCount(int[] arr) {
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