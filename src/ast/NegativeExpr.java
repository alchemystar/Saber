package ast;
import env.Environment;
import exception.SaberException;

import java.util.List;

public class NegativeExpr extends ASTList {
    public NegativeExpr(List<ASTree> c) { super(c); }
    public ASTree operand() { return child(0); }
    public String toString() {
        return "-" + operand();
    }

    public Object eval(Environment env) {
        Object v = (operand()).eval(env);
        if (v instanceof Integer)
            return new Integer(-((Integer)v).intValue());
        else
            throw new SaberException("bad type for -", this);
    }
}
