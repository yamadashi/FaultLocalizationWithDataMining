import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.util.List;
import java.util.ArrayList;

public class TraceInfoProcessor {
    public static void main(String[] args) {
        String inputfile = args[0];
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(inputfile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<Integer> passCounts = getPassCounts(reader);

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String outputfile = args[1];
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter((new File(outputfile)), true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Integer count : passCounts) {
            try {
                writer.write("," + count);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> getPassCounts(BufferedReader r) {
        List<Integer> passCounts = new ArrayList<>();
        String line = null;

        do {
            try {
                line = r.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) break; // ダサい、リファクタリングできそう

            String[] tmp = line.split(":", 3);
            if (tmp.length < 3)
                continue;

            // 行番号の取得(冗長かも)
            int lineNum = 0;
            try {
                lineNum = Integer.parseInt(tmp[1].trim());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (lineNum < 1)
                continue;

            // 行通過回数の取得
            int passCount = 0;
            try {
                passCount = Integer.parseInt(tmp[0].trim());
            } catch (NumberFormatException e) {
                // "-","#####"の時通過回数は0
                passCount = 0;
            }

            passCounts.add(passCount); // passCounts[i]はi+1行目の通過数

        } while (true);

        return passCounts;
    }
}