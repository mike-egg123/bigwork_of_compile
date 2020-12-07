package test;

import analyser.Analyser;
import analyser.InstructionEntry;
import analyser.SymbolEntry;
import error.AnalyzeError;
import error.CompileError;
import error.ErrorCode;
import tokenizer.StringIter;
import tokenizer.Tokenizer;
import util.Pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class InstructionsTest {
    static SymbolEntry[] functions = new SymbolEntry[100];
    static int funcCount = 0;
    static SymbolEntry[] vars = new SymbolEntry[100];
    static int varsCount = 0;
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
        tide(symbolTable);
        if(!an.hasMain()){
            throw new AnalyzeError(ErrorCode.MainFuncMissing,new Pos(0,0));
        }
    }
    public static void tide(HashMap<String, SymbolEntry> symbolTable){
        Iterator iter = symbolTable.entrySet().iterator();
        int globalCount = 0;
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            String name = entry.getKey().toString();
            SymbolEntry symbolEntry = (SymbolEntry) entry.getValue();
            //函数
            if(symbolEntry.getType().equals("func")){
                InstructionEntry[] instructionEntries = symbolEntry.getInstructions();
                int instructionLen = symbolEntry.getInstructionLen();
                System.out.println("函数 " + name + " :");
                for(int i = 0;i < instructionLen;i++){
                    System.out.println(instructionEntries[i].getInstru() + "(" + instructionEntries[i].getOpera() +")");
                }

            }
            else{
                System.out.print(String.format("变量 %s %s %s %d\n", name, symbolEntry.getType(), symbolEntry.getReturnType(), symbolEntry.getLayer()));
            }
            globalCount++;
        }
    }
}
