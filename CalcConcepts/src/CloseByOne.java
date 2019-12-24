import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiPredicate;

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
	private String filter = "none";
	private int conceptCount = 0; // 概念の数

	private final int targetIndex = 0;
	private final int negativeIndex = 1;

	private int[] upto = new int[INTSIZE];
	private int[][] cols = null; // ある属性をもつオブジェクトの集合

	private BiPredicate<int[], int[]> filterFunc = null;

	public CloseByOne(String file, String filter, float minSupRate) {
		this.filter = filter;
		this.minSupRate = minSupRate;
		context = readContext(file);
		prepare();
	}

	public void run() {

		Concept top = createTopConcept();

		long startTime = System.currentTimeMillis();
		generateFromNode(top, 2); // targetAttr(0, 1)はパスする
		System.out.println("#mining time = " + (System.currentTimeMillis() - startTime));

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
		System.out.println("#attr:" + attrNum + " " + "obj:" + objNum);
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

		for (int i = 0; i < INTSIZE; i++) {
			for (int j = 0; j < i; j++) {
				upto[i] |= (BIT << (INTSIZE - 1 - j));
			}
		}

		cols = new int[intAttrLen * INTSIZE][intObjLen];
		supps = new int[intAttrLen * INTSIZE];

		for (int i = 0; i < objNum; i++) {
			for (int j = 0; j < intAttrLen; j++) {
				for (int k = 0; k < INTSIZE; k++) {
					int mask = BIT << (INTSIZE - 1 - k);
					if ((context[i * intAttrLen + j] & mask) == 0)
						continue;
					cols[j * INTSIZE + k][i / INTSIZE] |= BIT << (INTSIZE - 1 - (i % INTSIZE));
					supps[j * INTSIZE + k]++;
				}
			}
		}

		switch (filter) {
		case "all":
		case "support":
			filterFunc = (int[] prevExt, int[] newExt) -> {
				int supp = SetOperation.size(SetOperation.intersection(newExt, cols[targetIndex]));
				return supp >= minSupport;
			};
			break;
		case "lift":
			filterFunc = (int[] prevExt, int[] newExt) -> true;
			break;
		case "none":
		default:
			filterFunc = (int[] prevExt, int[] newExt) -> SetOperation.size(newExt) >= minSupport;
			break;
		}
	}

	private void generateFromNode(Concept concept, int attrOffset) {

		int[] intent = concept.getIntent();
		for (int attr = attrOffset; attr < attrNum; attr++) {
			// 最後の属性に達したとき
			if (attr >= attrNum)
				return;
			// 追加する属性をすでに内包として持ってい場合、あるいは最小サポートを超えない場合
			int attrSupp = supps[attr];
			int mask = BIT << (INTSIZE - 1 - attr % INTSIZE);
			if ((intent[attr / INTSIZE] & mask) != 0 || attrSupp < minSupport) {
				continue;
			}

			Concept newConcept = computeClosure(concept, cols[attr]);
			boolean filterRes = filterFunc.test(concept.getExtent(), newConcept.getExtent());
			if (!filterRes)
				continue;
			// prefixを保存するか
			if (!newConcept.checkPPC(attr, concept, upto, cols[negativeIndex]))
				continue;

			conceptCount++;
			System.out.println(newConcept);

			generateFromNode(newConcept, attr + 1);
		}
	}

	private Concept createTopConcept() {
		int[] extent = new int[intObjLen];
		int[] intent = new int[intAttrLen];

		Arrays.fill(extent, 0);
		Arrays.fill(intent, BIT_MAX);

		for (int i = 0; i < intObjLen - 1; i++) {
			extent[i] = BIT_MAX;
		}
		for (int i = 0; i < objNum % INTSIZE; i++) {
			extent[intObjLen - 1] |= BIT << (INTSIZE - 1 - i);
		}

		for (int j = 0; j < objNum; j++) {
			for (int i = 0; i < intAttrLen; i++) {
				intent[i] &= context[intAttrLen * j + i];
			}
		}

		int[] targetSet = SetOperation.makeSet(intAttrLen, 0, 1);
		if (filter.equals("lift") || filter.equals("all"))
			return new ConceptWrapper(extent, intent, targetSet);
		else
			return new Concept(extent, intent, targetSet);

	}

	private Concept computeClosure(Concept prev, int[] attrExtent) {

		Concept newConcept = prev.clone();
		int[] extent = newConcept.getExtent();
		int[] intent = newConcept.getIntent();

		Arrays.fill(intent, BIT_MAX);

		for (int i = 0; i < intObjLen; i++) {
			extent[i] = prev.getExtent()[i] & attrExtent[i]; // 共通のオブジェクト
			if (extent[i] == 0) // 共通のオブジェクトがなかった場合
				continue;
			for (int j = 0; j < INTSIZE; j++) {
				if ((extent[i] & (BIT << (INTSIZE - 1 - j))) == 0)
					continue;
				int commonObj = i * INTSIZE + j;
				for (int k = 0; k < intAttrLen; k++) {
					intent[k] &= context[intAttrLen * commonObj + k]; // (k*INTSIZE+l)番目のオブジェクトが持ってない属性を除く
				}
			}
		}
		return newConcept;
	}

	public void printBit(int bit) {
		System.out.println(toStringBit(bit));
	}

	public String toStringBit(int bit) {
		return String.format("%32s", Integer.toBinaryString(bit)).replaceAll(" ", "0");
	}
}
