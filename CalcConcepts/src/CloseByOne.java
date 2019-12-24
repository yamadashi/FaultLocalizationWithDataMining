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
	private String filter = "none";
	private int conceptCount = 0; // 概念の数

	private final int targetIndex = 0;
	private final int negativeIndex = 1;

	private int[] upto = new int[INTSIZE];
	private int[][] cols = null; // ある属性をもつオブジェクトの集合

	public CloseByOne(String file, String filter, float minSupRate) {
		context = readContext(file);
		this.filter = filter;
		this.minSupRate = minSupRate;
		prepare();
	}

	public void run() {
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

	}

	private void calcConcepts() {
		Concept top = createTopConcept();

		long startTime = System.currentTimeMillis();
		generateFromNode(top, 0);
		System.out.println(" mining time = " + (System.currentTimeMillis() - startTime));
	}

	private void generateFromNode(Concept concept, int attrOffset) {
		int[] intent = concept.getIntent();
		ATTR: for (int attr = attrOffset; attr < attrNum; attr++) {
			// 最後の属性に達したとき
			if (attr >= attrNum)
				return;
			// 追加する属性をすでに内包として持ってい場合、あるいは最小サポートを超えない場合
			int supp = supps[attr];
			int mask = BIT << (INTSIZE - 1 - attr % INTSIZE);
			if ((intent[attr / INTSIZE] & mask) != 0 || supp < minSupport) {
				continue;
			}

			Concept newConcept = computeClosure(concept, cols[attr]);
			// 最小サポートを超えない場合

			if (calcSupp(newConcept) < minSupport) {
				continue;
			}
			int[] newIntent = newConcept.getIntent();
			// conceptとnewConceptにおいて、現在走査中の属性までの内包が一致していない場合
			for (int i = 0; i < attr / INTSIZE; i++) {
				if ((newIntent[i] ^ intent[i]) != 0) {
					continue ATTR;
				}
			}
			if (((newIntent[attr / INTSIZE] ^ intent[attr / INTSIZE]) & upto[attr % INTSIZE]) != 0) {
				continue;
			}

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
		return new Concept(extent, intent);
	}

	private Concept computeClosure(Concept prev, int[] attrExtent) {

		int[] extent = new int[intObjLen];
		int[] intent = new int[intAttrLen];

		Arrays.fill(extent, 0);
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
		Concept newConcept = new Concept(extent, intent);
		return newConcept;
	}

	private int calcSupp(Concept con) {
		if (filter.equals("support") | filter.equals("all"))
			return calcRuleSupp(con);
		else
			return SetOperation.size(con.getExtent());
	}

	private int calcRuleSupp(Concept con) {
		return SetOperation.size(SetOperation.intersection(con.getExtent(), cols[targetIndex]));
	}

	private float calcLift(Concept con) {
		int sup = calcRuleSupp(con);
		float conf = (float) sup / SetOperation.size(con.getExtent());
		float lift = conf / ((float) SetOperation.size(cols[targetIndex]) / objNum);
		return lift;
	}

	public void printBit(int bit) {
		System.out.println(toStringBit(bit));
	}

	public String toStringBit(int bit) {
		return String.format("%32s", Integer.toBinaryString(bit)).replaceAll(" ", "0");
	}
}
