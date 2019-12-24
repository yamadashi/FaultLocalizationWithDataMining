public class Main {
	public static void main(String[] args) {
		String file = args[0];
		String filter = args[1];
		float minsup = 0f;
		if (filter.equals("support") || filter.equals("all")) {
			minsup = Float.parseFloat(args[2]);
		}

		System.out.println("#target: " + file + ", filter:" + filter + ", minsup:" + minsup);
		new CloseByOne(file, filter, minsup).run();
	}
}
