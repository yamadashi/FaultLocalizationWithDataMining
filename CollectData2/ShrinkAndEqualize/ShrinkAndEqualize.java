import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.function.*;

public class ShrinkAndEqualize {

    public static void main(String[] args) {
        // reader
        String inputfile = args[0];
        Pair<Set<String>, Set<String>> res = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(inputfile)));
            res = shrinkAndDivide(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Set<String> passLines = res.first();
        Set<String> failLines = res.second();
        List<String> equalized = equalize(passLines, failLines);

        // writer
        String outputfile = args[1];
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(outputfile), false));
            for (String line : equalized) {
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Pair<Set<String>, Set<String>> shrinkAndDivide(BufferedReader reader) {
        Set<String> passLines = new HashSet<String>();
        Set<String> failLines = new HashSet<String>();
        Predicate<String> checkPass = line -> Integer.parseInt(line.split(" ", 2)[0]) == 1;
        String line = null;
        while (true) {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null)
                break;
                         
            if (checkPass.test(line)) passLines.add(line);
            else failLines.add(line);
        }
        return new Pair<Set<String>, Set<String>>(passLines, failLines);
    }

    private static List<String> equalize(Set<String> lines1, Set<String> lines2) {
        Set<String> smaller = null;
        Set<String> larger = null;
        if (lines1.size() > lines2.size()) {
            smaller = lines2;
            larger = lines1;
        }
        else {
            smaller = lines1;
            larger = lines2;
        }

        List<String> rtn = new ArrayList<String>();
        for (String line : smaller) {
            rtn.add(line);
        }
        for (String line : new ArrayList<String>(larger).subList(0,smaller.size())) {
            rtn.add(line);
        }

        return rtn;
    }
}
