package ast;
import env.Environment;

import java.util.List;

public class Fun extends ASTList {
    public Fun(List<ASTree> c) { super(c); }
    public ParameterList parameters() { return (ParameterList)child(0); }
    public BlockStmnt body() { return (BlockStmnt)child(1); }
    public Object eval(Environment env) {
        return new Function(parameters(), body(), env);
    }
    public String toString() {
        return "(fun " + parameters() + " " + body() + ")";
    }
}
