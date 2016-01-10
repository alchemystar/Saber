package ast;

import env.Environment;

import java.util.List;

/**
 * Created by alchemystar on 16/1/10.
 */
public class ReturnStmt extends ASTList {
    public ReturnStmt(List<ASTree> c) { super(c); }
    public ASTree resultExpr() { return child(0);}
    public Object eval(Environment env){
        System.out.println("haha111");
        return resultExpr().eval(env);
    }
}
