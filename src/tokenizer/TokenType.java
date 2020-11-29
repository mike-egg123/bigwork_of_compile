package tokenizer;

public enum TokenType {
    /** 空 */
    None,

    //字面量
    /** 无符号整数 */
    UINT_LITERAL,
    /** 字符串常量 */
    STRING_LITERAL,
    /** 浮点数常量 */
    DOUBLE_LITERAL,
    /** 字符常量 */
    CHAR_LITERAL,

    /** 标识符 */
    IDENT,

    //关键字
    /** fn */
    FN_KW,
    /** let */
    LET_KW,
    /** const */
    CONST_KW,
    /** as */
    AS_KW,
    /** while */
    WHILE_KW,
    /** if */
    IF_KW,
    /** else */
    ELSE_KW,
    /** return */
    RETURN_KW,
    /** break */
    BREAK_KW,
    /** continue */
    CONTINUE_KW,

    //运算符
    /** 加号+ */
    PLUS,
    /** 减号- */
    MINUS,
    /** 乘号* */
    MUL,
    /** 除号/ */
    DIV,
    /** 赋值符= */
    ASSIGN,
    /** 判断相等== */
    EQ,
    /** 判断不相等!= */
    NEQ,
    /** 小于号< */
    LT,
    /** 大于号> */
    GT,
    /** 小于等于<= */
    LE,
    /** 大于等于>= */
    GE,
    /** 左括号( */
    L_PAREN,
    /** 右括号) */
    R_PAREN,
    /** 左大括号{ */
    L_BRACE,
    /** 右大括号} */
    R_BRACE,
    /** 箭头-> */
    ARROW,
    /** 逗号, */
    COMMA,
    /** 冒号: */
    COLON,
    /** 分号; */
    SEMICOLON,

    /** 注释// */
    COMMENT,

    /** 文件尾 */
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case None:
                return "空";
            case UINT_LITERAL:
                return "无符号整数";
            case STRING_LITERAL:
                return "字符串常量";
            case DOUBLE_LITERAL:
                return "浮点数常量";
            case CHAR_LITERAL:
                return "字符常量";
            case IDENT:
                return "标识符";
            case FN_KW:
                return "fn";
            case LET_KW:
                return "let";
            case CONST_KW:
                return "const";
            case AS_KW:
                return "as";
            case WHILE_KW:
                return "while";
            case IF_KW:
                return "if";
            case ELSE_KW:
                return "else";
            case RETURN_KW:
                return "return";
            case BREAK_KW:
                return "break";
            case CONTINUE_KW:
                return "continue";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case MUL:
                return "*";
            case DIV:
                return "/";
            case ASSIGN:
                return "=";
            case EQ:
                return "==";
            case NEQ:
                return "!=";
            case LT:
                return "<";
            case GT:
                return ">";
            case LE:
                return "<=";
            case GE:
                return ">=";
            case L_PAREN:
                return "(";
            case R_PAREN:
                return ")";
            case L_BRACE:
                return "{";
            case R_BRACE:
                return "}";
            case ARROW:
                return "->";
            case COMMA:
                return ",";
            case COLON:
                return ":";
            case SEMICOLON:
                return ";";
            case COMMENT:
                return "注释";
            case EOF:
                return "EOF";
            default:
                return "InvalidToken";
        }
    }
}
