package ast;
import env.Environment;
import exception.SaberException;

import java.util.List;

public class ArrayRef extends Postfix {
    public ArrayRef(List<ASTree> c) {
        super(c);
    }

    public ASTree index() {
        return child(0);
    }

    public String toString() {
        return "[" + index() + "]";
    }

    /**
     * 对数组的引用,形似a[1]
     * 首先计算出下标表达式的值1
     * 然后对a指向的key中的数组object[]取出
     * 并通过java中object[index]的形式返回值
     * 至此,数组引用完毕
     * 语法结构为:identify{postfix}=>identify{[expr]}
     * 树形结构为:
     *      ----------identify-------
     *      |                       |
     * identify0<=>value        POSTFIX<=>ArrayRef
     *                              |
     *                              Expr
     * 这样child(0)便是expr=>index,POSTFIX.eval(env,getValue(identify0));
     */
    public Object eval(Environment env, Object value) {
        if (value instanceof Object[]) {
            Object index = index().eval(env);
            if (index instanceof Integer)
                return ((Object[]) value)[(Integer) index];
        }
        throw new SaberException("bad array access", this);

    }
}
