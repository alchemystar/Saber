package ast;
import env.Environment;

import java.util.List;

public class ArrayLiteral extends ASTList {
    public ArrayLiteral(List<ASTree> list) { super(list); }
    public int size() { return numChildren(); }

    /**
     * 形如a=[1,2,3]
     * 此函数步骤,取出子节点数量3个,new一个3个object的数组
     * 然后将3个子节点中的表达式计算出来,并写进数组里面
     * 这样,环境中就有了代表此数组的key
     * 至此,数组定义完毕
     */
    public Object eval(Environment env) {
        int s = numChildren();
        Object[] res = new Object[s];
        int i = 0;
        for (ASTree t: this)
            res[i++] = t.eval(env);
        return res;
    }
}
