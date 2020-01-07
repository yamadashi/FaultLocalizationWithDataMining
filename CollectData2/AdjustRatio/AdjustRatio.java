import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AdjustRatio {

    public static void main(String[] args) {
        // reader
        String inputfile = args[0];
        List<String> lines = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(inputfile)));
            lines = readInitialLines(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        List<String> failObj = getFailObj(lines);
        final int objNum = lines.size();
        final int increment = (int) (0.02 * objNum);

        while (calcRatio(lines) < 0.45) {
            List<String> add = randomSubList(failObj, increment);
            addFailObj(lines, add);
            removeHead(lines, increment);
        }
        if (lines.size() > objNum) {
            removeHead(lines, lines.size() - objNum);
        }
        System.out.println(calcRatio(lines));

        // writer
        String outputfile = args[1];
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(outputfile), false));
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static List<String> readInitialLines(BufferedReader reader) {
        List<String> lines = new LinkedList<String>();
        String line = null;
        while (true) {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null)
                break;

            lines.add(line);
        }
        return lines;
    }

    private static float calcRatio(List<String> lines) {
        int failNum = 0;
        for (String line : lines) {
            if (Integer.parseInt(line.split(" ", 2)[0]) == 0) {
                failNum++;
            }
        }
        return (float) failNum / lines.size();
    }

    private static List<String> getFailObj(List<String> lines) {
        List<String> failObj = new ArrayList<>();
        for (String line : lines) {
            if (Integer.parseInt(line.split(" ", 2)[0]) == 0) {
                failObj.add(line);
            }
        }
        return failObj;
    }

    private static void addFailObj(List<String> lines, List<String> fail) {
        lines.addAll(fail);
    }

    private static void removeHead(List<String> lines, int num) {
        lines.subList(0, num).clear();
    }

    private static List<String> randomSubList(List<String> list, int num) {
        if (num > list.size()) {
            List<String> rtn = new ArrayList<String>(list);
            rtn.addAll(randomSubList(list, num - list.size()));
            return rtn;
        } else {
            List<String> rtn = new ArrayList<String>(list);
            Collections.shuffle(rtn);
            return rtn.subList(0, num);
        }
    }
}
