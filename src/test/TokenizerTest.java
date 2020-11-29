package test;
import error.TokenizeError;
import tokenizer.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TokenizerTest {
    public static void main(String[] args) throws FileNotFoundException, TokenizeError {
        Token t = null;
        Scanner sc = new Scanner(new File(args[0]));
        StringIter it = new StringIter(sc);
        Tokenizer tn = new Tokenizer(it);
        while(true){
            System.out.println(tn.nextToken());
            if(it.isEOF()){
                break;
            }
        }
    }
}
