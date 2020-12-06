package tokenizer;

import error.TokenizeError;
import error.ErrorCode;
import util.Pos;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexDigit();
        }
        else if (Character.isAlphabetic(peek) || peek == '_') {
            return lexIdentOrKeyword();
        }
        else if(peek == '\"'){
            return lexStringLitteral();
        }
        else if(peek == '\''){
            return lexCharLitteral();
        }
        else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexCharLitteral() throws TokenizeError {
        Pos startPos = it.currentPos();
        char[] charstr =new char[10];
        char[] finstr = new char[10];
        int i = 0;
        int j = 0;
        it.nextChar();
        while (it.peekChar() != '\'') {
            charstr[i] = it.peekChar();
            it.nextChar();
            i++;
        }
        int len = i;
        if(len > 2 || len == 0){
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
        for(i = 0;i < len;i++){
            if(charstr[i] == '\\'){
                i++;
                switch (charstr[i]){
                    case '\'':
                        finstr[j++] = '\'';
                        continue;
                    case '\"':
                        finstr[j++] = '\"';
                        continue;
                    case '\\':
                        finstr[j++] = '\\';
                        continue;
                    case 'n':
                        finstr[j++] = '\n';
                        continue;
                    case 'r':
                        finstr[j++] = '\r';
                        continue;
                    case 't':
                        finstr[j++] = '\t';
                        continue;
                    default:
                        throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
            }
            else if(charstr[j] == '\r' || charstr[j] == '\n' || charstr[j] == '\t'){
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            }
            finstr[j++] = charstr[i];
        }
        String tmp = new String(finstr,0,j);
        Token t = new Token(TokenType.CHAR_LITERAL, tmp, startPos, it.currentPos());
        it.nextChar();
        return t;
    }

    private Token lexStringLitteral() throws TokenizeError{
        Pos startPos = it.currentPos();
        char[] charstr =new char[1000];
        char[] finstr = new char[1000];
        int i = 0;
        int j = 0;
        it.nextChar();
        while (it.peekChar() != '\"') {
            charstr[i] = it.peekChar();
            it.nextChar();
            i++;
        }
        int len = i;
        for(i = 0;i < len;i++){
            if(charstr[i] == '\\'){
                i++;
                switch (charstr[i]){
                    case '\'':
                        finstr[j++] = '\'';
                        continue;
                    case '\"':
                        finstr[j++] = '\"';
                        continue;
                    case '\\':
                        finstr[j++] = '\\';
                        continue;
                    case 'n':
                        finstr[j++] = '\n';
                        continue;
                    case 'r':
                        finstr[j++] = '\r';
                        continue;
                    case 't':
                        finstr[j++] = '\t';
                        continue;
                    default:
                        throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
            }
            else if(charstr[j] == '\r' || charstr[j] == '\n' || charstr[j] == '\t'){
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            }
            finstr[j++] = charstr[i];
        }
        String tmp = new String(finstr,0,j);
        Token t = new Token(TokenType.STRING_LITERAL, tmp, startPos, it.currentPos());
        it.nextChar();
        return t;
    }

    private Token lexDigit() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值
        Pos startPos = it.currentPos();
        char[] numstr =new char[50];
        int i = 0;
        boolean isDouble = false;
        while (Character.isDigit(it.peekChar()) || it.peekChar() == '.') {
            if(it.peekChar() == '.'){
                numstr[i] = it.nextChar();
                i++;
                while(Character.isDigit(it.peekChar()) || it.peekChar() == 'e' || it.peekChar() == 'E' || it.peekChar() == '-' || it.peekChar() == '+'){
                    numstr[i] = it.nextChar();
                    i++;
                }
                isDouble = true;
                break;
            }
            numstr[i] = it.nextChar();
            i++;
        }
        String tmp = new String(numstr,0,i);
        //System.out.println(tmp);
        if(isDouble){
            double numdouble = Double.parseDouble(tmp);
            return new Token(TokenType.DOUBLE_LITERAL, numdouble, startPos, it.currentPos());
        }
        else{
            int numint = Integer.parseInt(tmp);
            //System.out.println("hhh");
            return new Token(TokenType.UINT_LITERAL, numint, startPos, it.currentPos());
        }
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
        int i = 0;
        Pos startPos = it.currentPos();
        char[] ident_or_keyword_str = new char[50];
        while (Character.isDigit(it.peekChar()) || Character.isLetter(it.peekChar()) || it.peekChar() == '_') {
            ident_or_keyword_str[i] = it.nextChar();
            i++;
        }
        String tmp = new String(ident_or_keyword_str,0,i);
        switch (tmp){
            case "fn":
                return new Token(TokenType.FN_KW, tmp, startPos, it.currentPos());

            case "let":
                return new Token(TokenType.LET_KW, tmp, startPos, it.currentPos());

            case "const":
                return new Token(TokenType.CONST_KW, tmp, startPos, it.currentPos());

            case "as":
                return new Token(TokenType.AS_KW, tmp, startPos, it.currentPos());

            case "while":
                return new Token(TokenType.WHILE_KW, tmp, startPos, it.currentPos());

            case "if":
                return new Token(TokenType.IF_KW, tmp, startPos, it.currentPos());

            case "else":
                return new Token(TokenType.ELSE_KW, tmp, startPos, it.currentPos());

            case "return":
                return new Token(TokenType.RETURN_KW, tmp, startPos, it.currentPos());

            case "break":
                return new Token(TokenType.BREAK_KW, tmp, startPos, it.currentPos());

            case "continue":
                return new Token(TokenType.CONTINUE_KW, tmp, startPos, it.currentPos());
        }
        return new Token(TokenType.IDENT, tmp, startPos, it.currentPos());
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':
                // 填入返回语句
                if(it.peekChar() == '>'){//是箭头
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());

            case '*':
                // 填入返回语句
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            case '/':
                // 填入返回语句
                if(it.peekChar() == '/'){//是双等于号
                    while(it.nextChar() != '\n');
                    //return new Token(TokenType.COMMENT, "//", it.previousPos(), it.currentPos());
                    return nextToken();
                }
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());

            case '=':
                // 填入返回语句
                if(it.peekChar() == '='){//是双等于号
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());
            case '!':
                // 填入返回语句
                if(it.peekChar() == '='){//是不等于号
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }
                else{
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
            case '<':
                // 填入返回语句
                if(it.peekChar() == '='){//是双等于号
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());
            case '>':
                // 填入返回语句
                if(it.peekChar() == '='){//是双等于号
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());
            case '(':
                // 填入返回语句
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());

            case ')':
                // 填入返回语句
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());
            case '{':
                // 填入返回语句
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());
            case '}':
                // 填入返回语句
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());
            case ',':
                // 填入返回语句
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());
            case ':':
                // 填入返回语句
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());
            case ';':
                // 填入返回语句
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());

            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
