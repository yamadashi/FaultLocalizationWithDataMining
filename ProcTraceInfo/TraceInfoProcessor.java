import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.util.List;
import java.util.ArrayList;

public class TraceInfoProcessor {
    public static void main(String[] args) {
        String filename = args[0];
        BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
        List<Integer> passCounts = getPassCounts(reader);
        
        reader.close();
    }

    private static List<Integer> getPassCounts(BufferedReader r) {
        List<Integer> passCounts = new ArrayList<>();
        String line;
        while (line = r.readLine()) { //IOExceptionはとりあえず無視
            String[] tmp = line.split(":", 3);
            if (tmp.length < 3) continue;

            //行番号の取得(冗長かも)
            int lineNum = 0;
            try {
                lineNum = Integer.parseInt(tmp[1].trim());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (lineNum < 1) continue;

            //行通過回数の取得
            int passCount = 0;
            try {
                passCount = Integer.parseInt(tmp[0].trim());
            } catch(NumberFormatException e) {
                //"-"の時通過回数は0
                passCount = 0;
            }

            passCounts.add(passCount); //passCounts[i]はi+1行目の通過数
        }

        return passCounts;
    }
}