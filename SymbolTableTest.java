import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SymbolTableTest {
    public static void main(String[] args) throws FileNotFoundException, CompileError {
        Scanner sc = new Scanner(new File(args[0]));
        StringIter it = new StringIter(sc);
        Tokenizer tn = new Tokenizer(it);
        Analyser an = new Analyser(tn);
        an.analyse();
//        Map map = new HashMap();
//        Iterator iter = map.entrySet().iterator();
//        while (iter.hasNext()) {
//        Map.Entry entry = (Map.Entry) iter.next();
//        Object key = entry.getKey();
//        Object val = entry.getValue();

        HashMap<String, SymbolEntry> symbolTable = an.getSymbolTable();
        Iterator iter = symbolTable.entrySet().iterator();
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            String name = entry.getKey().toString();
            SymbolEntry symbolEntry = (SymbolEntry) entry.getValue();
            //SymbolEntry symbolEntry = symbolTable.get(symbolEntryIterator.next());
            System.out.print(String.format("%s %s %s %d\n", name, symbolEntry.getType(), symbolEntry.getReturnType(), symbolEntry.getLayer()));
        }
        if(!an.hasMain()){
            throw new AnalyzeError(ErrorCode.MainFuncMissing,new Pos(0,0));
        }
    }
}
