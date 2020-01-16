import java.util.*;
import java.io.*;
import java.util.function.*;

public class Main {

	private static int attrNum = 0, objNum = 0;
	private static int intObjLen = 0;
	private static final int INTSIZE = Integer.SIZE;
	private static final int targetIndex = 1;
	private static List<int[]> objSets;

	public static void main(String[] args) {
		String file = args[0];
		float minCooccurence = 0.01f * Integer.parseInt(args[1]);

		System.out.println("# min co-occurence : " + minCooccurence);

		objSets = getObjSets(file);
		System.out.println("# fail : " + SetOperation.size(objSets.get(0)));

		int co_occCount = 0; // 共起度が閾値を超える数
		// 行属性のみ共起度計算
		for (int i = 2; i < attrNum; i++) {
			float coocc = calcCooccurence(i);
			if (coocc >= minCooccurence) {
				co_occCount++;
			}
		}
		System.out.println("co-occurence : " + co_occCount);
	}

	private static List<int[]> getObjSets(String file) {

		ArrayList<int[]> buff = new ArrayList<int[]>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(file)));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split(" ");
				int[] attrIndices = new int[splitLine.length];
				for (int i = 0; i < attrIndices.length; i++) {
					attrIndices[i] = Integer.parseInt(splitLine[i]);
					if (attrNum < attrIndices[i]) {
						attrNum = attrIndices[i];
					}
				}
				buff.add(attrIndices);
			}
			objNum = buff.size();
			intObjLen = objNum / INTSIZE + 1;
			br.close();
		} catch (Exception e) {
			System.out.println("error:" + e);
		}

		List<int[]> objSets = new ArrayList<>();
		BiFunction<List<int[]>, Integer, int[]> getObjSet = (sets, attr) -> {
			if (attr >= sets.size()) {
				int addition = attr - sets.size() + 1;
				for (int i = 0; i < addition; i++) {
					sets.add(new int[intObjLen]);
				}
			}
			return sets.get(attr);
		};

		for (int i = 0; i < buff.size(); i++) {
			for (int attr : buff.get(i)) {
				SetOperation.add(getObjSet.apply(objSets, attr), i);
			}
		}

		return objSets;
	}

	private static float calcCooccurence(int a) {
		// || ext_fail ∩ ext_a || / || ext_a ||
		int numerator = SetOperation.size(SetOperation.intersection(objSets.get(a), objSets.get(targetIndex)));
		int denominator = SetOperation.size(objSets.get(a));
		float coocc = numerator != 0 ? (float) (numerator) / denominator : 0f;
		System.out.println((a - 1) + " : " + coocc);
		return coocc;
	}
}
