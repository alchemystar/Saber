package ast;
import env.Environment;

import java.util.List;

public class BlockStmnt extends ASTList {
    public BlockStmnt(List<ASTree> c) { super(c); }

    public Object eval(Environment env){
        Object result = 0;
        for(ASTree t:this){
            if(t instanceof ReturnStmt){
                return t.eval(env);
            }
            else if(!(t instanceof  NullStmnt)){
                result = t.eval(env);
            }
        }
        return result;
    }
}
