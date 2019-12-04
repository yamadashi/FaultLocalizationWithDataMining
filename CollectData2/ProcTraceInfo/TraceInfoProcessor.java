import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.NumberFormatException;

public class TraceInfoProcessor {
    static BufferedReader reader = null;
    static BufferedWriter writer = null;

    public static void main(String[] args) {

        // reader
        String inputfile = args[0];
        try {
            reader = new BufferedReader(new FileReader(new File(inputfile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // writer
        String outputfile = args[1];
        try {
            writer = new BufferedWriter(new FileWriter((new File(outputfile)), true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        makeContext();

        // close
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 実行トレースの通過行を取得
    private static void makeContext() {
        String line = null;
        do {
            try {
                line = reader.readLine();
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
            String passCountStr = tmp[0].trim();
            try {
                passed = Integer.parseInt(passCountStr) > 0;
            } catch (NumberFormatException e) {
                // "-","#####"の時通過回数は0
                passed = false;
            }

            try {
                if (passed) {
                    writer.write("1,"); // 0,1番目の属性はfail,passなので1行目は2番目の属性になる
                } else {
                    writer.write("0,");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } while (true);

        try {
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}