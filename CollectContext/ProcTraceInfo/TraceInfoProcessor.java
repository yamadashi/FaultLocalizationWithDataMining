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
        List<Integer> passLineNums = getPassLineNums(reader);

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

        for (Integer lineNum : passLineNums) {
            try {
                writer.write("," + lineNum);
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

    // 実行トレースの通過行を取得
    private static List<Integer> getPassLineNums(BufferedReader r) {
        List<Integer> passLineNums = new ArrayList<>();
        String line = null;

        do {
            try {
                line = r.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null)
                break; // ダサい、リファクタリングできそう

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

            // 行通過フラグ
            boolean passed = false;
            try {
                passed = Integer.parseInt(tmp[0].trim()) > 0;
            } catch (NumberFormatException e) {
                // "-","#####"の時通過回数は0
                passed = false;
            }

            if (passed)
                passLineNums.add(lineNum);

        } while (true);

        return passLineNums;
    }
}