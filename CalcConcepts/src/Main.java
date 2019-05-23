public class Main {
	public static void main(String[] args) {
		String file = args[0];
		int minsupp = 0;
		if (args.length > 1)
			minsupp = Integer.parseInt(args[1]);

		System.out.println("target: " + file);
		CloseByOne CbO = new CloseByOne(file, minsupp);
		CbO.main();
	}
}
