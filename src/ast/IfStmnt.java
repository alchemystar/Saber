package ast;
import env.Environment;

import java.util.List;

public class IfStmnt extends ASTList {
    public IfStmnt(List<ASTree> c) { super(c); }
    public ASTree condition() { return child(0); }
    public ASTree thenBlock() { return child(1); }
    public ASTree elseBlock() {
        return numChildren() > 2 ? child(2) : null;
    }

    public Object eval(Environment env){
        //首先计算条件表达式
        Object  c = condition().eval(env);
        //如果条件表达式为真,thenBlock()
        if(c instanceof Integer && ((Integer)c).intValue() != StaticVar.FALSE){
            return thenBlock().eval(env);
        }else{
            //为假,elseBlock();
            ASTree b=elseBlock();
            if(b == null){
                return 0;
            }else {
                return elseBlock().eval(env);
            }
        }
    }

    public String toString() {
        return "(if " + condition() + " " + thenBlock()
                 + " else " + elseBlock() + ")";
    }
}
