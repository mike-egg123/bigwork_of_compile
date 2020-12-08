import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    //层次
    int layer = 0;

    //是否有main函数
    boolean hasMain = false;

    public boolean hasMain(){
        return hasMain;
    }

    /** 符号表 */
    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();

    //索引表
    HashMap<String, Integer> funcIndex = new HashMap<>();
    int findex = 9;
    HashMap<String, Integer> globaVarIndex = new HashMap<>();
    int vindex = 0;

    public HashMap<String, Integer> getFuncIndex() {
        return funcIndex;
    }

    public void setFuncIndex(HashMap<String, Integer> funcIndex) {
        this.funcIndex = funcIndex;
    }

    public void initSymbolTable(){
        this.symbolTable.put("getint", new SymbolEntry("func", "int", new InstructionEntry[10], 0, true, true, getNextVariableOffset()));
        funcIndex.put("getint", 0);
        this.symbolTable.put("getdouble", new SymbolEntry("func", "double", new InstructionEntry[10], 0, true, true, getNextVariableOffset()));
        funcIndex.put("getdouble", 1);
        this.symbolTable.put("getchar", new SymbolEntry("func", "char", new InstructionEntry[10], 0, true, true, getNextVariableOffset()));
        funcIndex.put("getchar", 2);
        this.symbolTable.put("putint", new SymbolEntry("func", "void", new InstructionEntry[10], 0, true, true, getNextVariableOffset()));
        funcIndex.put("putint", 3);
        this.symbolTable.put("putdouble", new SymbolEntry("func", "void", new InstructionEntry[10], 0, true, true, getNextVariableOffset()));
        funcIndex.put("putdouble", 4);
        this.symbolTable.put("putchar", new SymbolEntry("func", "void", new InstructionEntry[10], 0, true, true, getNextVariableOffset()));
        funcIndex.put("putchar", 5);
        this.symbolTable.put("putstr", new SymbolEntry("func", "void", new InstructionEntry[10], 0, true, true, getNextVariableOffset()));
        funcIndex.put("putstr", 6);
        this.symbolTable.put("putln", new SymbolEntry("func", "void", new InstructionEntry[10], 0, true, true, getNextVariableOffset()));
        funcIndex.put("putln", 7);
        this.symbolTable.put("_start", new SymbolEntry("func", "void", new InstructionEntry[1000], 0, true, true, getNextVariableOffset()));
        funcIndex.put("_start", 8);
    }

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

    public HashMap<String, SymbolEntry> getSymbolTable(){
        return this.symbolTable;
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
    private void addSymbol(String name, String type, int layer, boolean isInitialized, boolean isConstant, Pos curPos) throws AnalyzeError {
        Iterator iter = symbolTable.entrySet().iterator();
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            String name1 = entry.getKey().toString();
            SymbolEntry symbolEntry1 = (SymbolEntry) entry.getValue();
            //SymbolEntry symbolEntry = symbolTable.get(symbolEntryIterator.next());
            //System.out.print(String.format("%s %s %d\n", name, symbolEntry.getType(), symbolEntry.getLayer()));
            if(name1.equals(name) && symbolEntry1.getLayer() == layer){
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration,curPos);
            }
        }
        this.symbolTable.put(name, new SymbolEntry(type, layer, isConstant, isInitialized, getNextVariableOffset()));
    }
    private void addSymbol(String name, String type, String returnType, int layer, boolean isInitialized, boolean isConstant, Pos curPos) throws AnalyzeError {
        Iterator iter = symbolTable.entrySet().iterator();
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            String name1 = entry.getKey().toString();
            SymbolEntry symbolEntry1 = (SymbolEntry) entry.getValue();
            //SymbolEntry symbolEntry = symbolTable.get(symbolEntryIterator.next());
            //System.out.print(String.format("%s %s %d\n", name, symbolEntry.getType(), symbolEntry.getLayer()));
            if(name1.equals(name) && symbolEntry1.getLayer() <= layer){
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration,curPos);
            }
        }
        this.symbolTable.put(name, new SymbolEntry(type, returnType, layer, isConstant, isInitialized, getNextVariableOffset()));
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
        initSymbolTable();
        while(!check(TokenType.EOF)){
            if(check(TokenType.FN_KW)){
                analyseFunction();
            }
            else if(check(TokenType.LET_KW)){
                analyseLetDeclStmt("_start", false);
            }
            else if(check(TokenType.CONST_KW)){
                analyseConstDeclStmt("_start", false);
            }
            else{
                throw new ExpectedTokenError(List.of(TokenType.FN_KW, TokenType.LET_KW, TokenType.CONST_KW), next());
            }
        }
        expect(TokenType.EOF);
        initStart();
        System.out.println("语法分析完成");
    }

    private int getIndexByName(String name){
        Iterator iter = symbolTable.entrySet().iterator();
        int i = 0;
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            String name1 = entry.getKey().toString();
            SymbolEntry symbolEntry1 = (SymbolEntry) entry.getValue();
            if(name1.equals(name)){
                return i;
            }
            i++;
        }
        return -1;
    }

    private void initStart(){
        SymbolEntry startEntry = symbolTable.get("_start");
        Iterator iter = symbolTable.entrySet().iterator();
        int j = startEntry.getInstructionLen();
        InstructionEntry[] instructionEntries = startEntry.getInstructions();
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            String name1 = entry.getKey().toString();
            SymbolEntry symbolEntry1 = (SymbolEntry) entry.getValue();
            if(symbolEntry1.getType().equals("func") && name1.equals("main")){
                InstructionEntry instructionEntry4 = new InstructionEntry("stackalloc", 0);
                InstructionEntry instructionEntry = new InstructionEntry("call", funcIndex.get(name1) - 8);
                instructionEntries[j++] = instructionEntry4;
                instructionEntries[j++] = instructionEntry;
            }
        }
        SymbolEntry start = symbolTable.get("_start");
        start.setInstructions(instructionEntries);
        start.setInstructionLen(j);
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
        String name = (String) nameToken.getValue();
        addSymbol(name, "func", "returnType", layer++,true, false, nameToken.getStartPos());
        expect(TokenType.L_PAREN);
        if(nextIf(TokenType.R_PAREN) == null){
            analyseFunctionParamList(name);
        }
        expect(TokenType.ARROW);
        //返回值类型，以后可能要改，加入符号表啥的
        String returnType = (String)expect(TokenType.IDENT).getValue();
        // 加入符号表

        if(name.equals("main")){
            hasMain = true;
        }
        String type = "func";
//        addSymbol(name,  type, returnType, layer++,true, false, nameToken.getStartPos());
        SymbolEntry thisSymbol = symbolTable.get(name);
        thisSymbol.setReturnType(returnType);
        funcIndex.put(name, findex++);
        analyseBlockStmt(name);
        if(returnType.equals("void")){
            SymbolEntry function = symbolTable.get(name);
            InstructionEntry[] instructionEntries = function.getInstructions();
            int len = function.getInstructionLen();
            InstructionEntry instructionEntry1 = new InstructionEntry("ret");
            instructionEntries[len++] = instructionEntry1;
            function.setInstructionLen(len);
            function.setInstructions(instructionEntries);
        }
        //将当前的变量弹出符号表
        int currentLayer = layer;
        Set<String> keys = symbolTable.keySet();
        Iterator<String> symbolEntryIterator = keys.iterator();
        while(symbolEntryIterator.hasNext()){
            SymbolEntry symbolEntry = symbolTable.get(symbolEntryIterator.next());
            if(symbolEntry.getLayer() == currentLayer){
                symbolEntryIterator.remove();
            }
        }
        layer = currentLayer - 1;

    }
    private void analyseFunctionParamList(String funcName) throws CompileError {
        if(check(TokenType.CONST_KW) || check(TokenType.IDENT)){
            analyseFunctionParam(funcName);
        }
        while(check(TokenType.COMMA)){
            expect(TokenType.COMMA);
            analyseFunctionParam(funcName);
        }
        expect(TokenType.R_PAREN);
    }
    private void analyseFunctionParam(String funcName) throws CompileError{
        boolean isconst = false;
        if(nextIf(TokenType.CONST_KW) != null){
            isconst = true;
        }
        var nameToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        String type = (String)expect(TokenType.IDENT).getValue();
        // 加入符号表
        String name = (String) nameToken.getValue();
        addSymbol(name, type, layer,true, isconst, nameToken.getStartPos());
        SymbolEntry thisSymbol = symbolTable.get(name);
        thisSymbol.setParam(true);
        SymbolEntry function = symbolTable.get(funcName);
        HashMap<String, Integer> argVars = function.getArgVars();
        int argVarsCount = function.getArgVarCount();
        argVars.put(name, argVarsCount++);
        function.setArgVars(argVars);
        function.setArgVarCount(argVarsCount);
    }
    private void analyseStmt(String funcName) throws CompileError{
        if(check(TokenType.R_BRACE)){
        }
        else{
            //变量声明语句
            if(check(TokenType.LET_KW)){
                analyseLetDeclStmt(funcName, true);
            }
            //常量声明语句
            else if(check(TokenType.CONST_KW)){
                analyseConstDeclStmt(funcName, true);
            }
            //if语句
            else if(check(TokenType.IF_KW)){
                analyseIfStmt(funcName);
            }
            //while语句
            else if(check(TokenType.WHILE_KW)){
                analyseWhileStmt(funcName);
            }
            //return语句
            else if(check(TokenType.RETURN_KW)){
                analyseReturnStmt(funcName);
            }
            //语句块
            else if(check(TokenType.L_BRACE)){
                analyseBlockStmt(funcName);
            }
            //空语句
            else if(check(TokenType.SEMICOLON)){
                analyseEmptyStmt();
            }
            //表达式语句
            else{
                analyseExprStmt(funcName);
            }
        }
    }
    private void analyseExprStmt(String funcName) throws CompileError{
        analyseExpr(funcName);
        expect(TokenType.SEMICOLON);

    }
    private void analyseEmptyStmt() throws CompileError{
        expect(TokenType.SEMICOLON);

    }
    private void analyseBlockStmt(String funcName) throws CompileError{
        expect(TokenType.L_BRACE);
//        if(nextIf(TokenType.R_BRACE) == null){
//            analyseStmt();
//        }
        while(!check(TokenType.R_BRACE)){
            analyseStmt(funcName);
        }
        expect(TokenType.R_BRACE);
    }
    private void analyseReturnStmt(String funcName) throws CompileError{
        boolean isInt = false;
        SymbolEntry symbolEntry = symbolTable.get(funcName);
        var nameToken = expect(TokenType.RETURN_KW);
        SymbolEntry function = symbolTable.get(funcName);
        InstructionEntry[] instructionEntries = function.getInstructions();
        //有返回值
        if(!check(TokenType.SEMICOLON)){
            int len = function.getInstructionLen();
            InstructionEntry instructionEntry1 = new InstructionEntry("arga", 0);
            instructionEntries[len++] = instructionEntry1;
            function.setInstructionLen(len);
            function.setInstructions(instructionEntries);
            isInt = true;
            String type = analyseExpr(funcName);
            instructionEntries = function.getInstructions();
            len = function.getInstructionLen();
            InstructionEntry instructionEntry2 = new InstructionEntry("store64");
            instructionEntries[len++] = instructionEntry2;
            function.setInstructionLen(len);
            function.setInstructions(instructionEntries);
            assert symbolEntry != null;
            if(!symbolEntry.getReturnType().equals(type)){
                throw new AnalyzeError(ErrorCode.ReturnTypeWrong, nameToken.getStartPos());
            }
        }
        assert symbolEntry != null;
        if(symbolEntry.getReturnType().equals("int")){
            if(!isInt){
                throw new AnalyzeError(ErrorCode.ReturnTypeWrong, nameToken.getStartPos());
            }
        }
        instructionEntries = function.getInstructions();
        int len = function.getInstructionLen();
        InstructionEntry instructionEntry3 = new InstructionEntry("ret");
        instructionEntries[len++] = instructionEntry3;
        function.setInstructionLen(len);
        function.setInstructions(instructionEntries);
        expect(TokenType.SEMICOLON);


    }

    private void insertInstru(String funcname, InstructionEntry instructionEntry, int pos){
        SymbolEntry function = symbolTable.get(funcname);
        InstructionEntry[] instructionEntries = function.getInstructions();
        int len = function.getInstructionLen();
        for(int i = len;i > pos;i--){
            instructionEntries[i] = instructionEntries[i - 1];
        }
        instructionEntries[pos] = instructionEntry;
        len++;
        function.setInstructionLen(len);
        function.setInstructions(instructionEntries);
    }

    private void analyseWhileStmt(String funcName) throws CompileError{
        expect(TokenType.WHILE_KW);
        SymbolEntry function = symbolTable.get(funcName);
        InstructionEntry[] instructionEntries = function.getInstructions();
        int len = function.getInstructionLen();
        InstructionEntry instructionEntry1 = new InstructionEntry("br", 0);
        instructionEntries[len++] = instructionEntry1;
        function.setInstructionLen(len);
        function.setInstructions(instructionEntries);
        //记录当前指令集位置为loc1
        int loc1 = function.getInstructionLen();
        analyseExpr(funcName);
        //记录当前指令集位置为loc2
        int loc2 = function.getInstructionLen();
        instructionEntries = function.getInstructions();
        if(!instructionEntries[loc2 - 1].getInstru().equals("brtrue") && !instructionEntries[loc2 - 1].getInstru().equals("brfalse")){
            insertInstru(funcName, new InstructionEntry("brtrue", 1), loc2++);
        }
        analyseBlockStmt(funcName);
        //记录当前指令集位置为loc3
        int loc3 = function.getInstructionLen();
        insertInstru(funcName, new InstructionEntry("br", loc3 - loc2 + 1), loc2);
        insertInstru(funcName, new InstructionEntry("br", loc1 - loc3 - 2), loc3 + 1);
    }
    private void analyseIfStmt(String funcName) throws CompileError{
        expect(TokenType.IF_KW);
        SymbolEntry function = symbolTable.get(funcName);
        analyseExpr(funcName);
        //loc1
        int loc1 = function.getInstructionLen();
        InstructionEntry[] instructionEntries = function.getInstructions();
        if(!instructionEntries[loc1 - 1].getInstru().equals("brtrue") && !instructionEntries[loc1 - 1].getInstru().equals("brfalse")){
            insertInstru(funcName, new InstructionEntry("brtrue", 1), loc1++);
        }
        analyseBlockStmt(funcName);
        //loc2
        int loc2 = function.getInstructionLen();
        insertInstru(funcName, new InstructionEntry("br", loc2 - loc1 + 1), loc1);
        //insertInstru(funcName, new InstructionEntry("br", 0), loc2 + 1);
        boolean hasElse = false;
        if(nextIf(TokenType.ELSE_KW) != null){
            hasElse = true;
            if(check(TokenType.L_BRACE)){
                analyseBlockStmt(funcName);
//                int loc3 = function.getInstructionLen();
//                insertInstru(funcName, new InstructionEntry("br", loc3 - loc2), loc2 + 1);
            }
            else if(check(TokenType.IF_KW)){
                analyseIfStmt(funcName);
                //int loc3 = function.getInstructionLen();
                //insertInstru(funcName, new InstructionEntry("br", loc3 - loc2), loc2 + 1);
            }
        }
        function = symbolTable.get(funcName);
        int loc3 = function.getInstructionLen();
        if(hasElse){
            insertInstru(funcName, new InstructionEntry("br", loc3 - loc2), loc2 + 1);
            loc3++;
        }
        insertInstru(funcName, new InstructionEntry("br", 0), loc3);
    }
    private void analyseConstDeclStmt(String funcName, boolean isLoca) throws CompileError{
        String locaOrglob = "globa";
        if(isLoca){
            locaOrglob = "loca";
        }
        SymbolEntry function = symbolTable.get(funcName);
        InstructionEntry[] instructionEntries = function.getInstructions();
        int len = function.getInstructionLen();
        int locaVarCount = function.getLocaVarCount();
        InstructionEntry instructionEntry1 = new InstructionEntry(locaOrglob, locaVarCount++);
        function.setLocaVarCount(locaVarCount);
        instructionEntries[len++] = instructionEntry1;
        function.setInstructionLen(len);
        function.setInstructions(instructionEntries);
        expect(TokenType.CONST_KW);
        var nameToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        String type = (String)expect(TokenType.IDENT).getValue();
        if(!(type.equals("int") || type.equals("double"))){
            throw new AnalyzeError(ErrorCode.InvalidAssignment, nameToken.getStartPos());
        }
        expect(TokenType.ASSIGN);
        analyseExpr(funcName);
        len = function.getInstructionLen();
        InstructionEntry instructionEntry2 = new InstructionEntry("store64");
        instructionEntries[len++] = instructionEntry2;
        function.setInstructionLen(len);
        function.setInstructions(instructionEntries);
        expect(TokenType.SEMICOLON);
        // 加入符号表
        String name = (String) nameToken.getValue();
        addSymbol(name, type, layer,true, true, nameToken.getStartPos());
        HashMap<String,Integer> localVars = function.getLocalVars();
        localVars.put(name, locaVarCount - 1);
        if(!isLoca){
            globaVarIndex.put(name, vindex++);
        }
    }
    private void analyseLetDeclStmt(String funcName, boolean isLoca) throws CompileError{
        boolean isInitialized = false;
        SymbolEntry function = symbolTable.get(funcName);
        expect(TokenType.LET_KW);
        var nameToken = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        String type = (String)expect(TokenType.IDENT).getValue();
        if(!(type.equals("int") || type.equals("double"))){
            throw new AnalyzeError(ErrorCode.InvalidAssignment, nameToken.getStartPos());
        }
        if(check(TokenType.ASSIGN)){
            isInitialized = true;
            expect(TokenType.ASSIGN);
            String locaOrglob = "globa";
            if(isLoca){
                locaOrglob = "loca";
            }
            InstructionEntry[] instructionEntries = function.getInstructions();
            int len = function.getInstructionLen();
            int locaVarCount = function.getLocaVarCount();
            InstructionEntry instructionEntry1 = new InstructionEntry(locaOrglob, locaVarCount++);
            function.setLocaVarCount(locaVarCount);
            instructionEntries[len++] = instructionEntry1;
            function.setInstructionLen(len);
            function.setInstructions(instructionEntries);
            analyseExpr(funcName);
            len = function.getInstructionLen();
            InstructionEntry instructionEntry2 = new InstructionEntry("store64");
            instructionEntries[len++] = instructionEntry2;
            function.setInstructionLen(len);
            function.setInstructions(instructionEntries);
        }
        else{
            int locaVarCount = function.getLocaVarCount();
            locaVarCount++;
            function.setLocaVarCount(locaVarCount);
        }
        expect(TokenType.SEMICOLON);

        // 加入符号表
        String name = (String) nameToken.getValue();
        addSymbol(name, type, layer, isInitialized, false, nameToken.getStartPos());
        HashMap<String,Integer> localVars = function.getLocalVars();
        localVars.put(name, function.getLocaVarCount() - 1);
        if(!isLoca){
            globaVarIndex.put(name, vindex++);
        }
    }
    /*
     * 改写表达式相关的产生式：
     * E -> C ( == | != | < | > | <= | >= C )
     * C -> T { + | - T}
     * T -> F { * | / F}
     * F -> A ( as int_ty | double_ty )
     * A -> ( - ) I
     * I -> IDENT | UNIT | DOUBLE | func_call | '(' E ')' | IDENT = E
     *  */
    private String analyseExpr(String funcName) throws CompileError {
        String type = analyseC(funcName);
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
            analyseC(funcName);

            SymbolEntry function = symbolTable.get(funcName);
            InstructionEntry[] instructionEntries = function.getInstructions();
            int len = function.getInstructionLen();
            // 生成代码
            if (op.getTokenType() == TokenType.EQ) {
                InstructionEntry instructionEntry1 = new InstructionEntry("cmpi");
                InstructionEntry instructionEntry2 = new InstructionEntry("brfalse", 1);
                instructionEntries[len++] = instructionEntry1;
                instructionEntries[len++] = instructionEntry2;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            } else if (op.getTokenType() == TokenType.NEQ) {
                InstructionEntry instructionEntry1 = new InstructionEntry("cmpi");
                InstructionEntry instructionEntry2 = new InstructionEntry("brtrue", 1);
                instructionEntries[len++] = instructionEntry1;
                instructionEntries[len++] = instructionEntry2;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            }else if (op.getTokenType() == TokenType.LT) {
                InstructionEntry instructionEntry1 = new InstructionEntry("cmpi");
                InstructionEntry instructionEntry2 = new InstructionEntry("setlt");
                InstructionEntry instructionEntry3 = new InstructionEntry("brtrue", 1);
                instructionEntries[len++] = instructionEntry1;
                instructionEntries[len++] = instructionEntry2;
                instructionEntries[len++] = instructionEntry3;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            }else if (op.getTokenType() == TokenType.GT) {
                InstructionEntry instructionEntry1 = new InstructionEntry("cmpi");
                InstructionEntry instructionEntry2 = new InstructionEntry("setgt");
                InstructionEntry instructionEntry3 = new InstructionEntry("brtrue", 1);
                instructionEntries[len++] = instructionEntry1;
                instructionEntries[len++] = instructionEntry2;
                instructionEntries[len++] = instructionEntry3;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            }else if (op.getTokenType() == TokenType.LE) {
                InstructionEntry instructionEntry1 = new InstructionEntry("cmpi");
                InstructionEntry instructionEntry2 = new InstructionEntry("setgt");
                InstructionEntry instructionEntry3 = new InstructionEntry("brfalse", 1);
                instructionEntries[len++] = instructionEntry1;
                instructionEntries[len++] = instructionEntry2;
                instructionEntries[len++] = instructionEntry3;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            }else if (op.getTokenType() == TokenType.GE) {
                InstructionEntry instructionEntry1 = new InstructionEntry("cmpi");
                InstructionEntry instructionEntry2 = new InstructionEntry("setlt");
                InstructionEntry instructionEntry3 = new InstructionEntry("brfalse", 1);
                instructionEntries[len++] = instructionEntry1;
                instructionEntries[len++] = instructionEntry2;
                instructionEntries[len++] = instructionEntry3;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            }
        }
        return type;
    }
    private String analyseC(String funcName) throws CompileError {
        String type = analyseT(funcName);
        while (true) {
            // 预读可能是运算符的 token
            var op = peek();
            if (op.getTokenType() != TokenType.PLUS &&
                    op.getTokenType() != TokenType.MINUS) {
                break;
            }
            // 运算符
            next();
            analyseT(funcName);
            SymbolEntry function = symbolTable.get(funcName);
            InstructionEntry[] instructionEntries = function.getInstructions();
            int len = function.getInstructionLen();
            // 生成代码
            if (op.getTokenType() == TokenType.PLUS) {
                InstructionEntry instructionEntry1 = new InstructionEntry("addi");
                instructionEntries[len++] = instructionEntry1;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            } else if (op.getTokenType() == TokenType.MINUS) {
                InstructionEntry instructionEntry1 = new InstructionEntry("subi");
                instructionEntries[len++] = instructionEntry1;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            }
        }
        return type;
    }
    private String analyseT(String funcName) throws CompileError {
        String type = analyseF(funcName);
        while (true) {
            // 预读可能是运算符的 token
            var op = peek();
            if (op.getTokenType() != TokenType.MUL &&
                    op.getTokenType() != TokenType.DIV) {
                break;
            }
            // 运算符
            next();
            analyseF(funcName);
            SymbolEntry function = symbolTable.get(funcName);
            InstructionEntry[] instructionEntries = function.getInstructions();
            int len = function.getInstructionLen();
            // 生成代码
            if (op.getTokenType() == TokenType.MUL) {
                InstructionEntry instructionEntry1 = new InstructionEntry("multi");
                instructionEntries[len++] = instructionEntry1;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            } else if (op.getTokenType() == TokenType.DIV) {
                InstructionEntry instructionEntry1 = new InstructionEntry("divi");
                instructionEntries[len++] = instructionEntry1;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
            }
        }
        return type;
    }
    private String analyseF(String funcName) throws CompileError {
        String type = analyseA(funcName);
        if(check(TokenType.AS_KW)) {
            expect(TokenType.AS_KW);
            expect(TokenType.IDENT);
        }
        return type;
    }
    private String analyseA(String funcName) throws CompileError {
        String type;
        int minusCount = 0;
        while(check(TokenType.MINUS)){
            minusCount++;
            expect(TokenType.MINUS);
        }
        type = analyseI(funcName);
        for(int i = 0;i < minusCount;i++){
            SymbolEntry function = symbolTable.get(funcName);
            InstructionEntry[] instructionEntries = function.getInstructions();
            int len = function.getInstructionLen();
            // 生成代码
            InstructionEntry instructionEntry1 = new InstructionEntry("negi");
            instructionEntries[len++] = instructionEntry1;
            function.setInstructionLen(len);
            function.setInstructions(instructionEntries);
        }
        return type;
    }
    private String analyseI(String funcName) throws CompileError {
        if(check(TokenType.IDENT)){
            Token nameToken = expect(TokenType.IDENT);
            String name = nameToken.getValueString();
            var entry = this.symbolTable.get(name);
            if (entry == null) {
                throw new AnalyzeError(ErrorCode.NotDeclared, nameToken.getStartPos());
            }
            //调用函数（解决一下标准库的问题）
            if(check(TokenType.L_PAREN)){
                if(!entry.getType().equals("func")){
                    throw new AnalyzeError(ErrorCode.NotDeclared, nameToken.getStartPos());
                }
                String callOrcallname = "call";
                boolean isLib = false;
                if(name.equals("getint") || name.equals("getdouble") || name.equals("getchar") || name.equals("putint") || name.equals("putchar") || name.equals("putdouble") || name.equals("putstr") || name.equals("putln")){
                    callOrcallname = "callname";
                    isLib = true;
                }
                expect(TokenType.L_PAREN);
                boolean hasParam = false;
                //有参数
                if(!check(TokenType.R_PAREN)){
                    hasParam = true;
                    SymbolEntry function = symbolTable.get(funcName);
                    if(entry.getReturnType().equals("void")){
                        InstructionEntry[] instructionEntries = function.getInstructions();
                        int len = function.getInstructionLen();
                        InstructionEntry instructionEntry1 = new InstructionEntry("stackalloc", 0);
                        instructionEntries[len++] = instructionEntry1;
                        function.setInstructionLen(len);
                        function.setInstructions(instructionEntries);
                        analyseCallParamList(funcName);
                    }
                    else if(entry.getReturnType().equals("int")){
                        InstructionEntry[] instructionEntries = function.getInstructions();
                        int len = function.getInstructionLen();
                        InstructionEntry instructionEntry1 = new InstructionEntry("stackalloc", 1);
                        instructionEntries[len++] = instructionEntry1;
                        function.setInstructionLen(len);
                        function.setInstructions(instructionEntries);
                        analyseCallParamList(funcName);
                    }
                }
                expect(TokenType.R_PAREN);
                String returnType = entry.getReturnType();
                if(returnType.equals("int") && !hasParam){
                    SymbolEntry function = symbolTable.get(funcName);
                    InstructionEntry[] instructionEntries = function.getInstructions();
                    int len = function.getInstructionLen();
                    // 生成代码
                    InstructionEntry instructionEntry1 = new InstructionEntry("stackalloc", 1);
                    instructionEntries[len++] = instructionEntry1;
                    InstructionEntry instructionEntry2;
                    if(isLib){
                        instructionEntry2 = new InstructionEntry(callOrcallname, funcIndex.get(name));
                    }
                    else{
                        instructionEntry2 = new InstructionEntry(callOrcallname, funcIndex.get(name) - 8);
                    }
                    instructionEntries[len++] = instructionEntry2;
                    function.setInstructionLen(len);
                    function.setInstructions(instructionEntries);
                }
                else if(returnType.equals("void") && !hasParam){
                    SymbolEntry function = symbolTable.get(funcName);
                    InstructionEntry[] instructionEntries = function.getInstructions();
                    int len = function.getInstructionLen();
                    // 生成代码
                    InstructionEntry instructionEntry1 = new InstructionEntry("stackalloc", 0);
                    instructionEntries[len++] = instructionEntry1;
                    InstructionEntry instructionEntry2;
                    if(isLib){
                        instructionEntry2 = new InstructionEntry(callOrcallname, funcIndex.get(name));
                    }
                    else{
                        instructionEntry2 = new InstructionEntry(callOrcallname, funcIndex.get(name) - 8);
                    }
                    instructionEntries[len++] = instructionEntry2;
                    function.setInstructionLen(len);
                    function.setInstructions(instructionEntries);
                }
                else{
                    SymbolEntry function = symbolTable.get(funcName);
                    InstructionEntry[] instructionEntries = function.getInstructions();
                    int len = function.getInstructionLen();
                    // 生成代码
                    InstructionEntry instructionEntry2;
                    if(isLib){
                        instructionEntry2 = new InstructionEntry(callOrcallname, funcIndex.get(name));
                    }
                    else{
                        instructionEntry2 = new InstructionEntry(callOrcallname, funcIndex.get(name) - 8);
                    }
                    instructionEntries[len++] = instructionEntry2;
                    function.setInstructionLen(len);
                    function.setInstructions(instructionEntries);
                }
                return returnType;
            }
            //赋值
            else if(check(TokenType.ASSIGN)){
                if(!entry.getType().equals("int")){
                    throw new AnalyzeError(ErrorCode.NotDeclared, nameToken.getStartPos());
                }
                if(entry.isConstant()){
                    throw new AnalyzeError(ErrorCode.AssignToConstant, nameToken.getStartPos());
                }
                expect(TokenType.ASSIGN);
                SymbolEntry function = symbolTable.get(funcName);
                InstructionEntry[] instructionEntries = function.getInstructions();
                int len = function.getInstructionLen();
                // 生成代码
                HashMap<String, Integer> localVars = function.getLocalVars();
                HashMap<String, Integer> argVars = function.getArgVars();
                if(entry.isParam){
                    if(function.getReturnType().equals("void")){
                        int thisIndex = argVars.get(name);
                        InstructionEntry instructionEntry1 = new InstructionEntry("arga", thisIndex - 1);
                        instructionEntries[len++] = instructionEntry1;
                    }
                    else{
                        int thisIndex = argVars.get(name);
                        InstructionEntry instructionEntry1 = new InstructionEntry("arga", thisIndex);
                        instructionEntries[len++] = instructionEntry1;
                    }
                }
                else{
                    try{
                        int thisIndex = localVars.get(name);
                        InstructionEntry instructionEntry1 = new InstructionEntry("loca", thisIndex);
                        instructionEntries[len++] = instructionEntry1;
                    }catch (NullPointerException n){
                        int thisIndex = globaVarIndex.get(name);
                        InstructionEntry instructionEntry1 = new InstructionEntry("globa", thisIndex);
                        instructionEntries[len++] = instructionEntry1;
                    }
                }
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
                String type = analyseExpr(funcName);
                if(type.equals("void")){
                    throw new AnalyzeError(ErrorCode.InvalidAssignment, nameToken.getStartPos());
                }
                len = function.getInstructionLen();
                // 生成代码
                InstructionEntry instructionEntry2 = new InstructionEntry("store64");
                instructionEntries[len++] = instructionEntry2;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
                return "void";
            }
            //变量名
            else{
                SymbolEntry function = symbolTable.get(funcName);
                InstructionEntry[] instructionEntries = function.getInstructions();
                int len = function.getInstructionLen();
                // 生成代码
                HashMap<String, Integer> localVars = function.getLocalVars();
                HashMap<String, Integer> argVars = function.getArgVars();
                if(entry.isParam){
                    if(function.getReturnType().equals("void")){
                        int thisIndex = argVars.get(name);
                        InstructionEntry instructionEntry1 = new InstructionEntry("arga", thisIndex - 1);
                        instructionEntries[len++] = instructionEntry1;
                    }
                    else{
                        int thisIndex = argVars.get(name);
                        InstructionEntry instructionEntry1 = new InstructionEntry("arga", thisIndex);
                        instructionEntries[len++] = instructionEntry1;
                    }
                }
                else{
                    try{
                        int thisIndex = localVars.get(name);
                        InstructionEntry instructionEntry1 = new InstructionEntry("loca", thisIndex);
                        instructionEntries[len++] = instructionEntry1;
                    }catch (NullPointerException n){
                        int thisIndex = globaVarIndex.get(name);
                        InstructionEntry instructionEntry1 = new InstructionEntry("globa", thisIndex);
                        instructionEntries[len++] = instructionEntry1;
                    }
                }
                InstructionEntry instructionEntry2 = new InstructionEntry("load64");
                instructionEntries[len++] = instructionEntry2;
                function.setInstructionLen(len);
                function.setInstructions(instructionEntries);
                return entry.getType();
            }
        }
        else if(check(TokenType.UINT_LITERAL)){
            var token = expect(TokenType.UINT_LITERAL);
            SymbolEntry function = symbolTable.get(funcName);
            InstructionEntry[] instructionEntries = function.getInstructions();
            int len = function.getInstructionLen();
            // 生成代码
            InstructionEntry instructionEntry1 = new InstructionEntry("push", (int)token.getValue());
            instructionEntries[len++] = instructionEntry1;
            function.setInstructionLen(len);
            function.setInstructions(instructionEntries);
            return "int";
        }
        else if(check(TokenType.STRING_LITERAL)){
            var token = expect(TokenType.STRING_LITERAL);
            String value = (String)token.getValue();
            //计算全局变量数
            int globalVarsNum = calcGlobalVars();
            SymbolEntry function = symbolTable.get(funcName);
            InstructionEntry[] instructionEntries = function.getInstructions();
            int len = function.getInstructionLen();
            // 生成代码
            InstructionEntry instructionEntry1 = new InstructionEntry("push", globalVarsNum);
            instructionEntries[len++] = instructionEntry1;
            function.setInstructionLen(len);
            function.setInstructions(instructionEntries);
            //加入符号表
            addSymbol(value, "string", "returnType", 0,true, true, token.getStartPos());
            return "int";
        }
        else if(check(TokenType.DOUBLE_LITERAL)){
            expect(TokenType.DOUBLE_LITERAL);
            return "double";
        }
        else if(check(TokenType.L_PAREN)){
            expect(TokenType.L_PAREN);
            String type = analyseExpr(funcName);
            expect(TokenType.R_PAREN);
            return type;
        }
        return "null";
    }
    private void analyseCallParamList(String funcName) throws CompileError{
        analyseExpr(funcName);
        while(check(TokenType.COMMA)){
            expect(TokenType.COMMA);
            analyseExpr(funcName);
        }
    }
    private int calcGlobalVars(){
        int globalVars = 0;
        Iterator iter = symbolTable.entrySet().iterator();
        while(iter.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)iter.next();
            SymbolEntry symbolEntry = (SymbolEntry) entry.getValue();
            if(!symbolEntry.getType().equals("func") && symbolEntry.getLayer() == 0){
                globalVars++;
            }
        }
        return globalVars;
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