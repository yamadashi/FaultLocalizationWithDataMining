import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;


public class TraceInfoProcessor {
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));
        
        reader.close();
    }
}