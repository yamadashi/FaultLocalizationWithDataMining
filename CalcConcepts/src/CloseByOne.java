import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

class CloseByOne {
	static final int BIT_MAX = Integer.MAX_VALUE * 2 + 1;
	static final int INTSIZE = Integer.SIZE;
	static final int BIT = 1;

	private int objNum = 0; // オブジェクト数
	private int attrNum = 0; // 属性数
	private int intObjLen = 0; // 属性をbitで管理したときに何個intが必要か
	private int intAttrLen = 0; // オブジェクトをbitで管理したときに何個intが必要か
	private int[] context = null; // 文脈
	private int[] supps = null; // 各属性のサポート

	private float minSupRate = 0.00f; // オブジェクト数の何％を最小サポートとするか
	private int minSupport = 1; // 最小サポート
	private int conceptCount = 0; // 概念の数

	private int[] upto = new int[INTSIZE];
	private int[][] cols = null; // ある属性をもつオブジェクトの集合

	public CloseByOne(String file, int minSupRate) {
		context = readContext(file);
		this.minSupRate = 0.01f * minSupRate;
		prepare();
	}

	public void main() {
		calcConcepts();
		System.out.println("#concept = " + conceptCount);
	}

	// 入力ファイルから文脈データを読み取る
	private int[] readContext(String file) {

		ArrayList<int[]> buff = new ArrayList<int[]>();
		int maxAttrIndex = 0;

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(file)));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split(" ");
				int[] attrIndices = new int[splitLine.length];
				for (int i = 0; i < attrIndices.length; i++) {
					attrIndices[i] = Integer.parseInt(splitLine[i]);
					if (maxAttrIndex < attrIndices[i]) {
						maxAttrIndex = attrIndices[i];
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
		minSupport = (int) (minSupRate * objNum);
		if (minSupport == 0) {
			minSupport = 1;
		}
		System.out.println("attr:" + attrNum + "\n" + "obj:" + objNum);
		intObjLen = objNum / INTSIZE + 1;
		intAttrLen = attrNum / INTSIZE + 1;

		int[] context = new int[objNum * intAttrLen];
		for (int i = 0; i < objNum; i++) {
			int[] obj = buff.get(i);
			for (int j = 0; j < obj.length; j++) {
				context[i * intAttrLen + obj[j] / INTSIZE] |= BIT << (INTSIZE - (obj[j] % INTSIZE) - 1);
			}
		}
		return context;
	}

	private void prepare() {

		Concept.setProperties(intObjLen, intAttrLen);

		for (int i = 0; i < INTSIZE; i++) {
			for (int j = INTSIZE - 1; j > i; j--) {
				upto[i] |= (BIT << j);
			}
		}

		cols = new int[intAttrLen * INTSIZE][intObjLen];
		supps = new int[intAttrLen * INTSIZE];

		for (int i = 0; i < intAttrLen; i++) {
			for (int j = 0; j < INTSIZE; j++) {
				int mask = (BIT << j);
				for (int x = 0, y = i; x < objNum; x++, y += intAttrLen) {
					if ((context[y] & mask) != 0) {
						int attr = i * INTSIZE + (INTSIZE - j - 1);
						cols[attr][x / INTSIZE] |= BIT << (x % INTSIZE);
						supps[attr]++;
					}
				}
			}
		}
	}

	private void calcConcepts() {
		Concept initial = Concept.createVoidConcept();
		Concept top = computeClosure(initial, null).getFirst();

		long startTime = System.currentTimeMillis();
		generateFromNode(top, 0, INTSIZE - 1);
		System.out.println(" mining time = " + (System.currentTimeMillis() - startTime));
	}

	private void generateFromNode(Concept concept, int start_int, int start_bit) {
		int[] intent = concept.getIntent();
		for (; start_int < intAttrLen; start_int++) {
			ATTR: for (; start_bit >= 0; start_bit--) {
				int current = start_int * INTSIZE + (INTSIZE - 1 - start_bit);
				// 最後の属性に到達したとき
				if (current >= attrNum) {
					return;
				}
				// 属性をすでに内包として持っている場合、あるいは最小サポートを超えない場合
				int supp = supps[current];
				if (((intent[start_int] & (BIT << start_bit))) != 0 || (supp < minSupport)) {
					continue;
				}
				Pair<Concept, Integer> res = computeClosure(concept, cols[current]);
				Concept newConcept = res.getFirst();
				int newSupp = res.getSecond().intValue();
				int[] newIntent = newConcept.getIntent();
				// conceptとnewConceptにおいて、現在走査中の属性までの内包が一致していない場合
				for (int i = 0; i < start_int; i++) {
					if ((newIntent[i] ^ intent[i]) != 0) {
						continue ATTR;
					}
				}
				if (((newIntent[start_int] ^ intent[start_int]) & upto[start_bit]) != 0) {
					continue;
				}
				// 最小サポートを超えない場合
				if (newSupp < minSupport) {
					continue;
				}

				conceptCount++;
				Concept.printConcept(newConcept);

				if (start_bit == 0) {
					generateFromNode(newConcept, start_int + 1, INTSIZE - 1);
				} else {
					generateFromNode(newConcept, start_int, start_bit - 1);
				}
			}
			start_bit = INTSIZE - 1;
		}
	}

	private Pair<Concept, Integer> computeClosure(Concept prev, int[] attrExtent) {

		Concept rtn = Concept.createVoidConcept();
		int[] extent = rtn.getExtent();
		int[] intent = rtn.getIntent();

		Arrays.fill(extent, 0);
		Arrays.fill(intent, BIT_MAX);

		if (attrExtent == null) { // 初期状態としてルート(トップ)の概念を返す
			for (int i = 0; i < intObjLen; ++i) {
				extent[i] = BIT_MAX;
			}
			for (int j = 0; j < objNum; ++j) {
				for (int i = 0; i < intAttrLen; ++i)
					intent[i] &= context[intAttrLen * j + i];
			}
			return new Pair<Concept, Integer>(rtn, 0);
		} else {
			int supp = 0;
			for (int k = 0; k < intObjLen; ++k) {
				extent[k] = prev.getExtent()[k] & attrExtent[k]; // 共通のオブジェクト
				if (extent[k] != 0) { // 共通のオブジェクトがあった場合
					for (int l = 0; l < INTSIZE; ++l) {
						if ((extent[k] & (BIT << l)) != 0) { // (k*INTSIZE+l)番目のオブジェクトが共通
							for (int i = 0, j = intAttrLen * (k * INTSIZE + l); i < intAttrLen; ++i, ++j) {
								intent[i] &= context[j]; // (k*INTSIZE+l)番目のオブジェクトが持ってない属性を除く
							}
							++supp;
						}
					}
				}
			}
			return new Pair<Concept, Integer>(rtn, supp);
		}
	}

	public void printBit(int bit) {
		System.out.println(toStringBit(bit));
	}

	public String toStringBit(int bit) {
		return String.format("%32s", Integer.toBinaryString(bit)).replaceAll(" ", "0");
	}
}
