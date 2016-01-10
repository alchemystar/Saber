package ast;
import env.Environment;

import java.util.List;

public class WhileStmnt extends ASTList {
    public WhileStmnt(List<ASTree> c) { super(c); }
    public ASTree condition() { return child(0); }
    public ASTree body() { return child(1); }

    public Object eval(Environment env){
        Object result = 0;
        for(;;){
            Object c = condition().eval(env);
            //如果计算条件表达式为false,则返回
            if(c instanceof Integer && ((Integer)c).intValue() == StaticVar.FALSE){
                return result;
            }else{
                result = body().eval(env);
            }
        }
    }

    public String toString() {
        return "(while " + condition() + " " + body() + ")";
    }
}
