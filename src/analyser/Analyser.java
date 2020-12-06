package analyser;

import error.AnalyzeError;
import error.CompileError;
import error.ErrorCode;
import error.ExpectedTokenError;
import error.TokenizeError;
import instruction.Instruction;
import instruction.Operation;
import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;
import util.Pos;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;
    Token peeked2Token = null;

    /** 符号表 */
    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();

    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        //System.out.println(token.getTokenType());
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移
     *
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    /**
     * 添加一个符号
     *
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, Pos curPos) throws AnalyzeError {
        if (this.symbolTable.get(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            this.symbolTable.put(name, new SymbolEntry(isConstant, isInitialized, getNextVariableOffset()));
        }
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void initializeSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getStackOffset();
        }
    }

    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    /**
     * program -> decl_stmt* function*
     */
    private void analyseProgram() throws CompileError {
        while(!check(TokenType.EOF)){
            if(check(TokenType.FN_KW)){
                analyseFunction();
            }
            else if(check(TokenType.LET_KW)){
                analyseLetDeclStmt();
            }
            else if(check(TokenType.CONST_KW)){
                analyseConstDeclStmt();
            }
            else{
                throw new ExpectedTokenError(List.of(TokenType.FN_KW, TokenType.LET_KW, TokenType.CONST_KW), next());
            }
        }
        expect(TokenType.EOF);
        System.out.println("语法分析完成");
    }
    /*
    * function_param -> 'const'? IDENT ':' ty
      function_param_list -> function_param (',' function_param)*
      function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
    *  */
    private void analyseFunction() throws CompileError {
        expect(TokenType.FN_KW);
        //函数名
        var nameToken = expect(TokenType.IDENT);

        // 加入符号表
        String name = (String) nameToken.getValue();
        addSymbol(name, true, false, nameToken.getStartPos());
        expect(TokenType.L_PAREN);
        if(nextIf(TokenType.R_PAREN) == null){
            analyseFunctionParamList();
        }
        expect(TokenType.ARROW);
        //返回值，以后可能要改，加入符号表啥的
        expect(TokenType.IDENT);
        analyseBlockStmt();
    }
    private void analyseFunctionParamList() throws CompileError {
        if(check(TokenType.CONST_KW) || check(TokenType.IDENT)){
            analyseFunctionParam();
        }
        while(check(TokenType.COMMA)){
            expect(TokenType.COMMA);
            analyseFunctionParam();
        }
        expect(TokenType.R_PAREN);
    }
    private void analyseFunctionParam() throws CompileError{
        boolean isconst = false;
        if(nextIf(TokenType.CONST_KW) != null){
            isconst = true;
        }
        var nameToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        expect(TokenType.IDENT);
        // 加入符号表
        String name = (String) nameToken.getValue();
        addSymbol(name, true, isconst, nameToken.getStartPos());

    }
    private void analyseStmt() throws CompileError{
        if(check(TokenType.R_BRACE)){
        }
        else{
            //变量声明语句
            if(check(TokenType.LET_KW)){
                analyseLetDeclStmt();
            }
            //常量声明语句
            else if(check(TokenType.CONST_KW)){
                analyseConstDeclStmt();
            }
            //if语句
            else if(check(TokenType.IF_KW)){
                analyseIfStmt();
            }
            //while语句
            else if(check(TokenType.WHILE_KW)){
                analyseWhileStmt();
            }
            //return语句
            else if(check(TokenType.RETURN_KW)){
                analyseReturnStmt();
            }
            //语句块
            else if(check(TokenType.L_BRACE)){
                analyseBlockStmt();
            }
            //空语句
            else if(check(TokenType.SEMICOLON)){
                analyseEmptyStmt();
            }
            //表达式语句
            else{
                analyseExprStmt();
            }
        }
    }
    private void analyseExprStmt() throws CompileError{
        analyseExpr();
        expect(TokenType.SEMICOLON);

    }
    private void analyseEmptyStmt() throws CompileError{
        expect(TokenType.SEMICOLON);

    }
    private void analyseBlockStmt() throws CompileError{
        expect(TokenType.L_BRACE);
//        if(nextIf(TokenType.R_BRACE) == null){
//            analyseStmt();
//        }
        while(!check(TokenType.R_BRACE)){
            analyseStmt();
        }
        expect(TokenType.R_BRACE);
    }
    private void analyseReturnStmt() throws CompileError{
        expect(TokenType.RETURN_KW);
        if(!check(TokenType.SEMICOLON)){
            analyseExpr();
        }
        expect(TokenType.SEMICOLON);

    }
    private void analyseWhileStmt() throws CompileError{
        expect(TokenType.WHILE_KW);
        analyseExpr();
        analyseBlockStmt();
    }
    private void analyseIfStmt() throws CompileError{
        expect(TokenType.IF_KW);
        analyseExpr();
        analyseBlockStmt();
        if(nextIf(TokenType.ELSE_KW) != null){
            if(check(TokenType.L_BRACE)){
                analyseBlockStmt();
            }
            else if(check(TokenType.IF_KW)){
                analyseIfStmt();
            }
        }
    }
    private void analyseConstDeclStmt() throws CompileError{
        expect(TokenType.CONST_KW);
        var nameToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        expect(TokenType.IDENT);
        expect(TokenType.ASSIGN);
        analyseExpr();
        expect(TokenType.SEMICOLON);

        // 加入符号表
        String name = (String) nameToken.getValue();
        addSymbol(name, true, true, nameToken.getStartPos());
    }
    private void analyseLetDeclStmt() throws CompileError{
        boolean isInitialized = false;
        expect(TokenType.LET_KW);
        var nameToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        expect(TokenType.IDENT);
        if(check(TokenType.ASSIGN)){
            expect(TokenType.ASSIGN);
            analyseExpr();
            isInitialized = true;
        }
        expect(TokenType.SEMICOLON);

        // 加入符号表
        String name = (String) nameToken.getValue();
        addSymbol(name, isInitialized, false, nameToken.getStartPos());
    }
    /*
     * 改写表达式相关的产生式：
     * E -> C ( == | != | < | > | <= | >= C )
     * C -> T { + | - T}
     * T -> F { * | / F}
     * F -> A ( as int_ty | double_ty )
     * A -> ( - ) I
     * I -> IDENT | UNIT | DOUBLE | func_call | '(' E ')'
     *
     * E -> IDENT = E
     *  */
    private void analyseExpr() throws CompileError {
        analyseC();
        while (true) {
            // 预读可能是运算符的 token
            var op = peek();
            if (op.getTokenType() != TokenType.EQ &&
                    op.getTokenType() != TokenType.NEQ &&
                    op.getTokenType() != TokenType.LT &&
                    op.getTokenType() != TokenType.GT &&
                    op.getTokenType() != TokenType.LE &&
                    op.getTokenType() != TokenType.GE) {
                break;
            }
            // 运算符
            next();
            analyseC();
            // 生成代码
//            if (op.getTokenType() == TokenType.Plus) {
//                instructions.add(new Instruction(Operation.ADD));
//            } else if (op.getTokenType() == TokenType.Minus) {
//                instructions.add(new Instruction(Operation.SUB));
//            }
        }
    }
    private void analyseC() throws CompileError {
        analyseT();
        while (true) {
            // 预读可能是运算符的 token
            var op = peek();
            if (op.getTokenType() != TokenType.PLUS &&
                    op.getTokenType() != TokenType.MINUS) {
                break;
            }
            // 运算符
            next();
            analyseT();
            // 生成代码
            if (op.getTokenType() == TokenType.PLUS) {
                instructions.add(new Instruction(Operation.ADD));
            } else if (op.getTokenType() == TokenType.MINUS) {
                instructions.add(new Instruction(Operation.SUB));
            }
        }
    }
    private void analyseT() throws CompileError {
        analyseF();
        while (true) {
            // 预读可能是运算符的 token
            var op = peek();
            if (op.getTokenType() != TokenType.MUL &&
                    op.getTokenType() != TokenType.DIV) {
                break;
            }
            // 运算符
            next();
            analyseF();
            // 生成代码
            if (op.getTokenType() == TokenType.MUL) {
                instructions.add(new Instruction(Operation.MUL));
            } else if (op.getTokenType() == TokenType.DIV) {
                instructions.add(new Instruction(Operation.DIV));
            }
        }
    }
    private void analyseF() throws CompileError {
        analyseA();
        if(check(TokenType.AS_KW)) {
            expect(TokenType.AS_KW);
            expect(TokenType.IDENT);
        }
    }
    private void analyseA() throws CompileError {
        if(check(TokenType.MINUS)){
            expect(TokenType.MINUS);
        }
        analyseI();
    }
    private void analyseI() throws CompileError {
        if(check(TokenType.IDENT)){
            expect(TokenType.IDENT);
            if(check(TokenType.L_PAREN)){
                expect(TokenType.L_PAREN);
                if(!check(TokenType.R_PAREN)){
                    analyseCallParamList();
                }
                expect(TokenType.R_PAREN);
            }
            else if(check(TokenType.ASSIGN)){
                expect(TokenType.ASSIGN);
                analyseExpr();
            }
        }
        else if(check(TokenType.UINT_LITERAL)){
            expect(TokenType.UINT_LITERAL);
        }
        else if(check(TokenType.DOUBLE_LITERAL)){
            expect(TokenType.DOUBLE_LITERAL);
        }
        else if(check(TokenType.L_PAREN)){
            expect(TokenType.L_PAREN);
            analyseExpr();
            expect(TokenType.R_PAREN);
        }
    }
    private void analyseCallParamList() throws CompileError{
        analyseExpr();
        while(check(TokenType.COMMA)){
            expect(TokenType.COMMA);
            analyseExpr();
        }
    }






//    private void analyseMain() throws CompileError {
//        analyseConstantDeclaration();
//        analyseVariableDeclaration();
//        analyseStatementSequence();
//
//    }
//
//    private void analyseConstantDeclaration() throws CompileError {
//        // 示例函数，示例如何解析常量声明
//        // 常量声明 -> 常量声明语句*
//
//        // 如果下一个 token 是 const 就继续
//        while (nextIf(TokenType.Const) != null) {
//            // 常量声明语句 -> 'const' 变量名 '=' 常表达式 ';'
//
//            // 变量名
//            var nameToken = expect(TokenType.Ident);
//
//            // 加入符号表
//            String name = (String) nameToken.getValue();
//            addSymbol(name, true, true, nameToken.getStartPos());
//
//            // 等于号
//            expect(TokenType.Equal);
//
//            // 常表达式
//            var value = analyseConstantExpression();
//
//            // 分号
//            expect(TokenType.Semicolon);
//
//            // 这里把常量值直接放进栈里，位置和符号表记录的一样。
//            // 更高级的程序还可以把常量的值记录下来，遇到相应的变量直接替换成这个常数值，
//            // 我们这里就先不这么干了。
//            instructions.add(new Instruction(Operation.LIT, value));
//        }
//    }
//
//    private void analyseVariableDeclaration() throws CompileError {
//        // 示例函数，示例如何解析变量声明
//        // 变量声明 -> 变量声明语句*
//
//        // 如果下一个 token 是 var 就继续
//        while (nextIf(TokenType.Var) != null) {
//            // 变量声明语句 -> 'var' 变量名 ('=' 表达式)? ';'
//
//            // 变量名
//            var nameToken = expect(TokenType.Ident);
//
//            // 变量初始化了吗
//            boolean initialized = false;
//
//            // 下个 token 是等于号吗？如果是的话分析初始化
//
//            // 分析初始化的表达式
//            if(nextIf(TokenType.Equal) != null){
//                initialized = true;
//                //表达式
//                analyseExpression();
//            }
//
//            // 分号
//            expect(TokenType.Semicolon);
//
//            // 加入符号表，请填写名字和当前位置（报错用）
//            String name = /* 名字 */ (String) nameToken.getValue();;
//            addSymbol(name, initialized, false, /* 当前位置 */ nameToken.getStartPos());
//
//            // 如果没有初始化的话在栈里推入一个初始值
//            if (!initialized) {
//                instructions.add(new Instruction(Operation.LIT, 0));
//            }
//        }
//    }
//
//    private void analyseStatementSequence() throws CompileError {
//        // 语句序列 -> 语句*
//        // 语句 -> 赋值语句 | 输出语句 | 空语句
//
//        while (true) {
//            // 如果下一个 token 是……
//            var peeked = peek();
//            //赋值语句
//            if (peeked.getTokenType() == TokenType.Ident) {
//                // 调用相应的分析函数
//                // 如果遇到其他非终结符的 FIRST 集呢？
////                var nameToken = next();
////                //等号
////                expect(TokenType.Equal);
////                //表达式
////
////                //分号
////                expect(TokenType.Semicolon);
//                analyseAssignmentStatement();
//            }
//            //输出语句
//            else if(peeked.getTokenType() == TokenType.Print){
////                //PRINT
////                next();
////                //左括号
////                expect(TokenType.Lparen);
////                //表达式
////
////                //右括号
////                expect(TokenType.Rparen);
////                //分号
////                expect(TokenType.Semicolon);
//                analyseOutputStatement();
//            }
//            //空语句
//            else if(peeked.getTokenType() == TokenType.Semicolon){
//                //分号
//                next();
//            }
//            else {
//                // 都不是，摸了
//                break;
//            }
//        }
//    }
//
//    private int analyseConstantExpression() throws CompileError {
//        // 常表达式 -> 符号? 无符号整数
//        boolean negative = false;
//        if (nextIf(TokenType.Plus) != null) {
//            negative = false;
//        } else if (nextIf(TokenType.Minus) != null) {
//            negative = true;
//        }
//
//        var token = expect(TokenType.Uint);
//
//        int value = (int) token.getValue();
//        if (negative) {
//            value = -value;
//        }
//
//        return value;
//    }
//
//    private void analyseExpression() throws CompileError {
//        // 表达式 -> 项 (加法运算符 项)*
//        // 项
//        analyseItem();
//
//        while (true) {
//            // 预读可能是运算符的 token
//            var op = peek();
//            if (op.getTokenType() != TokenType.Plus && op.getTokenType() != TokenType.Minus) {
//                break;
//            }
//
//            // 运算符
//            next();
//
//            // 项
//            analyseItem();
//
//            // 生成代码
//            if (op.getTokenType() == TokenType.Plus) {
//                instructions.add(new Instruction(Operation.ADD));
//            } else if (op.getTokenType() == TokenType.Minus) {
//                instructions.add(new Instruction(Operation.SUB));
//            }
//        }
//    }
//
//    private void analyseAssignmentStatement() throws CompileError {
//        // 赋值语句 -> 标识符 '=' 表达式 ';'
//
//        // 分析这个语句
//
//        // 标识符是什么？
//        var nameToken = expect(TokenType.Ident);
//        String name = (String)nameToken.getValue();
//        var symbol = symbolTable.get(name);
//        if (symbol == null) {
//            // 没有这个标识符
//            throw new AnalyzeError(ErrorCode.NotDeclared, /* 当前位置 */ nameToken.getStartPos());
//        } else if (symbol.isConstant) {
//            // 标识符是常量
//            throw new AnalyzeError(ErrorCode.AssignToConstant, /* 当前位置 */ nameToken.getStartPos());
//        }
//        // 设置符号已初始化
//        initializeSymbol(name, nameToken.getStartPos());
//        expect(TokenType.Equal);
//        analyseExpression();
//        expect(TokenType.Semicolon);
//        // 把结果保存
//        var offset = getOffset(name, nameToken.getStartPos());
//        instructions.add(new Instruction(Operation.STO, offset));
//
//
//    }
//
//    private void analyseOutputStatement() throws CompileError {
//        // 输出语句 -> 'print' '(' 表达式 ')' ';'
//
//        expect(TokenType.Print);
//        expect(TokenType.LParen);
//
//        analyseExpression();
//
//        expect(TokenType.RParen);
//        expect(TokenType.Semicolon);
//
//        instructions.add(new Instruction(Operation.WRT));
//    }
//
//    private void analyseItem() throws CompileError {
//        // 项 -> 因子 (乘法运算符 因子)*
//
//        // 因子
//        analyseFactor();
//
//        while (true) {
//            // 预读可能是运算符的 token
//            var op = peek();
//            if (op.getTokenType() != TokenType.Mult && op.getTokenType() != TokenType.Div) {
//                break;
//            }
//
//            // 运算符
//            next();
//
//            // 因子
//            analyseFactor();
//
//            // 生成代码
//            if (op.getTokenType() == TokenType.Mult) {
//                instructions.add(new Instruction(Operation.MUL));
//            } else if (op.getTokenType() == TokenType.Div) {
//                instructions.add(new Instruction(Operation.DIV));
//            }
//        }
//    }
//
//    private void analyseFactor() throws CompileError {
//        // 因子 -> 符号? (标识符 | 无符号整数 | '(' 表达式 ')')
//
//        boolean negate;
//        if (nextIf(TokenType.Minus) != null) {
//            negate = true;
//            // 计算结果需要被 0 减
//            instructions.add(new Instruction(Operation.LIT, 0));
//        } else {
//            nextIf(TokenType.Plus);
//            negate = false;
//        }
//
//        if (check(TokenType.Ident)) {
//            // 是标识符
//            var nameToken = next();
//
//            // 加载标识符的值
//            String name = /* 快填 */ (String)nameToken.getValue();
//            var symbol = symbolTable.get(name);
//            if (symbol == null) {
//                // 没有这个标识符
//                throw new AnalyzeError(ErrorCode.NotDeclared, /* 当前位置 */ nameToken.getStartPos());
//            } else if (!symbol.isInitialized) {
//                // 标识符没初始化
//                throw new AnalyzeError(ErrorCode.NotInitialized, /* 当前位置 */ nameToken.getStartPos());
//            }
//            var offset = getOffset(name, nameToken.getStartPos());
//            instructions.add(new Instruction(Operation.LOD, offset));
//        } else if (check(TokenType.Uint)) {
//            // 是整数
//            // 加载整数值
//            var myInt = next();
//            int value = (int)myInt.getValue();
//            instructions.add(new Instruction(Operation.LIT, value));
//        } else if (check(TokenType.LParen)) {
//            next();
//            // 是表达式
//            // 调用相应的处理函数
//            analyseExpression();
//            expect(TokenType.RParen);
//        } else {
//            // 都不是，摸了
//            throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.LParen), next());
//        }
//
//        if (negate) {
//            instructions.add(new Instruction(Operation.SUB));
//        }
//    }
}