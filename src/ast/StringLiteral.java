package ast;


import env.Environment;
import lexer.Token;

public class StringLiteral extends ASTLeaf {
    public StringLiteral(Token t) { super(t); }
    public String value() { return token().getText(); }

    //其计算的时候就是返回其值
    public Object eval(Environment env){
       return value();
    }
}
