import java.util.Comparator;

public class MyDFSComparator implements Comparator<Couple> {

	public int compare(Couple obj1, Couple obj2) {
	int[] intent1 = obj1.getConcept().getIntent();
	int[] intent2 = obj2.getConcept().getIntent();

    int INTSIZE = Constants.INTSIZE;
    int ARCHBIT = INTSIZE - 1;  // cbo

    int bitVal1;
    int bitVal2;
	for(int i = 0; i < intent1.length; i++) {
		for(int pos = ARCHBIT; pos >= 0; pos--) {
			bitVal1 = getBit(intent1[i], pos);
			bitVal2 = getBit(intent2[i], pos);
			if(bitVal1 < bitVal2) {
				return 1;
			} else if(bitVal1 > bitVal2) {
				return -1;
			} else{
				continue;
			}
		}
	}
	return 0;
}

	public int getBit(int bit, int position)
	{
	   return (bit >> position) & 1;
	}
}
