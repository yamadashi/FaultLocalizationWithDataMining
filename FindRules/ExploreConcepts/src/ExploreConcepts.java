
// TODO
// extentのビット順入れ替えたい prepare computeclosure readContext

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.ArrayDeque;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class ExploreConcepts {

    private List<Pair<Concept, Pair<Integer, Float>>> solution;
    private Queue<Triplet> exploration;

    static public final int BIT_MAX = Integer.MAX_VALUE * 2 + 1;
    static public final int INTSIZE = Integer.SIZE;
    static public final int BIT = 1;
    private int[] context = null; // 文脈
    private int objNum = 0; // オブジェクト数
    private int attrNum = 0; // 属性数
    private int intObjLen = 0; // 属性をbitで管理したときに何個intが必要か
    private int intAttrLen = 0; // オブジェクトをbitで管理したときに何個intが必要か
    private int[] supps = null; // 各属性のサポート
    private int[] upto = new int[INTSIZE];
    private int[][] objHas = null; // ある属性をもつオブジェクトの集合

    private int minsupp; // 最小support
    private float minconf; // 最小confidence
    private int targetIndex; // ルールの後件となる属性のインデックス

    public ExploreConcepts(String file, int minsupp, float minconf, int targetIndex) {

        this.minsupp = minsupp;
        this.minconf = minconf;
        this.targetIndex = targetIndex;

        solution = new ArrayList<>();
        exploration = new ArrayDeque<Triplet>();

        readContext(file);
        prepare();
    }

    public List<Pair<Concept, Pair<Integer, Float>>> solve() {

        // 初期状態
        Concept top = computeClosure(null, null);

        for (Triplet tri : getChildren(top, 0)) {
            exploration.add(tri);
        }

        while (!exploration.isEmpty()) {
            Triplet triplet = exploration.poll();
            Concept s = triplet.getMap().getChild();

            Pair<Pair<Boolean, Boolean>, Pair<Integer, Float>> filterRes = FILTER(s.getExtent());

            Pair<Boolean, Boolean> flags = filterRes.getFirst();
            boolean KEEP = flags.getFirst();
            boolean CONTINUE = flags.getSecond();

            if (KEEP || CONTINUE) {

                if (KEEP) {
                    Pair<Integer, Float> info = filterRes.getSecond();
                    solution.add(new Pair<>(s, info));
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

    private Pair<Pair<Boolean, Boolean>, Pair<Integer, Float>> FILTER(int[] extent) {

        int[] tmp = extent.clone();
        for (int i = 0; i < intObjLen; i++) {
            tmp[i] &= objHas[targetIndex][i];
        }
        int sup = bitCount(tmp); // || ext_s ∩ ext_T ||
        float conf = (float) sup / bitCount(extent);

        boolean KEEP = sup >= minsupp && conf >= minconf;
        boolean CONTINUE = sup >= minsupp;

        return new Pair<>(new Pair<>(KEEP, CONTINUE), new Pair<>(sup, conf));
    }

    // int[] の立っているbit数を数える
    private int bitCount(int[] arr) {
        int rtn = 0;
        for (int e : arr) {
            rtn += Integer.bitCount(e);
        }
        return rtn;
    }

    private void readContext(String file) {

        ArrayList<Set<Integer>> buff = new ArrayList<Set<Integer>>();
        int maxAttrIndex = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(",");
                Set<Integer> attrIndices = new HashSet<>();
                for (int i = 0; i < splitLine.length; i++) {
                    int attrIndice = Integer.parseInt(splitLine[i]);
                    attrIndices.add(attrIndice);
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

        objNum = buff.size();
        attrNum = maxAttrIndex + 1; // indexは0からなので
        System.out.println("attr:" + attrNum + "\n" + "obj:" + objNum);
        intObjLen = objNum / INTSIZE + 1;
        intAttrLen = attrNum / INTSIZE + 1;

        context = new int[objNum * intAttrLen];
        for (int i = 0; i < objNum; i++) {
            Set<Integer> obj = buff.get(i);
            for (Integer index : obj) {
                context[i * intAttrLen + index / INTSIZE] |= BIT << (INTSIZE - (index % INTSIZE) - 1);
            }
        }
    }

    private void prepare() {

        for (int i = 0; i < INTSIZE; i++) {
            for (int j = INTSIZE - 1; j > i; j--) {
                upto[i] |= (BIT << j);
            }
        }

        objHas = new int[intAttrLen * INTSIZE][intObjLen];
        supps = new int[intAttrLen * INTSIZE];

        for (int i = 0; i < intAttrLen; i++) {
            for (int j = 0; j < INTSIZE; j++) {
                int mask = (BIT << j);
                for (int x = 0, y = i; x < objNum; x++, y += intAttrLen) {
                    if ((context[y] & mask) != 0) {
                        int attr = i * INTSIZE + (INTSIZE - j - 1);
                        objHas[attr][x / INTSIZE] |= BIT << (x % INTSIZE);
                        supps[attr]++;
                    }
                }
            }
        }
    }

    private Concept computeClosure(Concept prev, int[] attrExtent) {

        Concept rtn = createVoidConcept();
        int[] extent = rtn.getExtent();
        int[] intent = rtn.getIntent();

        Arrays.fill(extent, 0);
        Arrays.fill(intent, BIT_MAX);

        if (attrExtent == null) { // 初期状態としてルート(トップ)の概念を返す
            for (int i = 0; i < intObjLen - 1; ++i) {
                extent[i] = BIT_MAX;
            }
            for (int i = 0; i < objNum % INTSIZE; i++) {
                extent[intObjLen - 1] |= (BIT << i);
            }

            for (int j = 0; j < objNum; ++j) {
                for (int i = 0; i < intAttrLen; ++i)
                    intent[i] &= context[intAttrLen * j + i];
            }
        } else {
            for (int k = 0; k < intObjLen; ++k) {
                extent[k] = prev.getExtent()[k] & attrExtent[k]; // 共通のオブジェクト
                if (extent[k] != 0) { // 共通のオブジェクトがあった場合
                    for (int l = 0; l < INTSIZE; ++l) {
                        if ((extent[k] & (BIT << l)) != 0) { // (k*INTSIZE+l)番目のオブジェクトが共通
                            for (int i = 0, j = intAttrLen * (k * INTSIZE + l); i < intAttrLen; ++i, ++j) {
                                intent[i] &= context[j]; // (k*INTSIZE+l)番目のオブジェクトが持ってない属性を除く
                            }
                        }
                    }
                } else
                    return null;
            }
        }
        return rtn;
    }

    private List<Triplet> getChildren(Concept par, int attrOffset) {
        List<Triplet> children = new ArrayList<Triplet>();
        List<Mapping> increments = new ArrayList<Mapping>();
        if (attrOffset >= attrNum)
            return null;

        System.out.print("par:");
        par.print();
        ATTR: for (int i = attrOffset; i < attrNum; i++) {
            // 属性をすでに内包として持っている場合
            if ((par.getIntent()[i / INTSIZE] & (BIT << (i % INTSIZE))) != 0) {
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
                    & upto[INTSIZE - (i % INTSIZE) - 1]) != 0) {
                continue;
            }

            System.out.print("  diff:" + i + " child:");
            child.print();
            increments.add(new Mapping(child, i));
        }

        for (Mapping mapping : increments) {
            children.add(new Triplet(mapping, par.getIntent(), increments));
        }

        if (children.size() == 0)
            return null;
        return children;
    }

    private Concept createVoidConcept() {
        Concept concept = new Concept(new int[intObjLen], new int[intAttrLen]);
        return concept;
    }

    public void printBit(int bit) {
        System.out.println(toStringBit(bit));
    }

    public String toStringBit(int bit) {
        return String.format("%32s", Integer.toBinaryString(bit)).replaceAll(" ", "0");
    }
}