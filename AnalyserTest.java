import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class AnalyserTest {
    public static void main(String[] args) throws FileNotFoundException, CompileError {
        Scanner sc = new Scanner(new File(args[0]));
        StringIter it = new StringIter(sc);
        Tokenizer tn = new Tokenizer(it);
        Analyser an = new Analyser(tn);
        an.analyse();
    }
}
