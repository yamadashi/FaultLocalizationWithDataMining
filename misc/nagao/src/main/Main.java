package main;
//-Xrunhprof:heap=sites,file=sites.txt

//-Xrunhprof:cpu=times,file=cpu_times.txt

public class Main {
	public static void main(String[] args) {// file mode level threads
		// System.out.println("cbo: file mode dl threads minsupp(%)");
		String file = "";
		file = "src/mushroom.txt";
		int mode = 6;// この部分の設定が優先なのでCloseByOne.java内を書き換えても無効
		int level = 2;// Krajca手法における探索の深さに対する制限
		int threads = 4;
		int minsupp = 0;// 0~100%で指定
		if (args.length != 0) {
			if (args[0].equals("0")) {
				file = "src/ttt_fixed.txt";
			} else if (args[0].equals("1")) {
				file = "src/mushroom.txt";
			} else if (args[0].equals("2")) {
				file = "src/anonymous.txt";
			} else if (args[0].equals("3")) {
				file = "src/connect.txt";
			} else if (args[0].equals("4")) {
				file = "src/adult.txt";
			} else if (args[0].equals("5")) {
				file = "src/adult_c.txt";
			} // 量的属性を削除したもの
			else if (args[0].equals("6")) {
				file = "src/ad5.txt";
			} // 量的属性を区間5つで等間隔に離散化したもの
			else {
				file = args[0];
			}
			mode = Integer.parseInt(args[1]);
			level = Integer.parseInt(args[2]);
			threads = Integer.parseInt(args[3]);
			minsupp = Integer.parseInt(args[4]);
		}

		System.out.println(" target: " + file);
		CloseByOne CbO = new CloseByOne(file, mode, level, threads);
		CbO.setMinSupp(minsupp);
		CbO.main();
		System.out.println(" end;");
	}
}
