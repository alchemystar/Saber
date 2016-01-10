package ast;
import env.Environment;
import exception.SaberException;

import java.util.List;

public class BinaryExpr extends ASTList {
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    public BinaryExpr(List<ASTree> c) {
        super(c);
    }

    public ASTree left() {
        return child(0);
    }

    public String operator() {
        return ((ASTLeaf) child(1)).token().getText();
    }

    public ASTree right() {
        return child(2);
    }

    public Object eval(Environment env) {
        String op = operator();
        if ("=".equals(op)) {
            //此处,将right的表达式计算之后赋值给左值
            Object right = right().eval(env);
            return computeAssign(env,right);
        } else {
            //左值
            Object left = left().eval(env);
            //右值
            Object right = right().eval(env);
            return computeOp(left,op,right);
        }
    }


    protected Object computeAssign(Environment env, Object rvalue) {
        ASTree le = left();
        if (le instanceof Name) {
            env.put(((Name) le).name(), rvalue);
            return rvalue;
        }
        else if (le instanceof PrimaryExpr) {
            /**
             * 如果赋值左边是arrayRef即a[1]这种情况
             * 先从a[1]的ast中获得a,再计算index1
             * 然后(object[] a)[index]=rvalue;
             */
            PrimaryExpr p = (PrimaryExpr)le;
            if (p.hasPostfix(0) && p.postfix(0) instanceof ArrayRef) {
                Object a = ((PrimaryExpr)le).evalSubExpr(env, 1);
                if (a instanceof Object[]) {
                    ArrayRef aref = (ArrayRef)p.postfix(0);
                    Object index = aref.index().eval(env);
                    if (index instanceof Integer) {
                        ((Object[])a)[(Integer)index] = rvalue;
                        return rvalue;
                    }
                }
            }
            throw new SaberException("bad array access", this);
        }
        else
            throw new SaberException("bad assignment", this);
    }


    /**
     * 当前只计算数字的加法 以及 String的加法
     * @param left
     * @param op
     * @param right
     * @return
     */
    protected Object computeOp(Object left, String op, Object right) {
        if (left instanceof Integer && right instanceof Integer) {
            return computeNumber((Integer)left, op, (Integer)right);
        }
        else
        if (op.equals("+"))
            return String.valueOf(left) + String.valueOf(right);
        else if (op.equals("==")) {
            if (left == null)
                return right == null ? TRUE : FALSE;
            else
                return left.equals(right) ? TRUE : FALSE;
        }
        else
            throw new SaberException("bad type", this);
    }

    protected Object computeNumber(Integer left, String op, Integer right) {
        int a = left.intValue();
        int b = right.intValue();
        if (op.equals("+"))
            return a + b;
        else if (op.equals("-"))
            return a - b;
        else if (op.equals("*"))
            return a * b;
        else if (op.equals("/"))
            return a / b;
        else if (op.equals("%"))
            return a % b;
        else if (op.equals("=="))
            return a == b ? TRUE : FALSE;
        else if (op.equals(">"))
            return a > b ? TRUE : FALSE;
        else if (op.equals("<"))
            return a < b ? TRUE : FALSE;
        else  if(op.equals("<="))
            return a <= b ? TRUE : FALSE;
        else if(op.equals(">="))
            return a >= b ? TRUE : FALSE;
        else
            throw new SaberException("bad operator", this);
    }
}
