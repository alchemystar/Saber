package ast;


import env.Environment;
import exception.SaberException;
import lexer.Token;

public class Name extends ASTLeaf {
    public Name(Token t) { super(t); }
    public String name() { return token().getText(); }

    public Object eval(Environment env){
        //获取对应名字的value
        Object value = env.get(name());
        if(value == null){
            throw new SaberException("this name undefined:"+name(),this);
        }else{
            return value;
        }
    }
}
