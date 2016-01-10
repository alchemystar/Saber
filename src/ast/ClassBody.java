package ast;
import env.Environment;

import java.util.List;

public class ClassBody extends ASTList {
    public ClassBody(List<ASTree> c) { super(c); }

    /**
     * ClassBody中定义函数,以及初始化变量等操作,
     * 事实上就是运行ClassBody中的AST树
     * @param env
     * @return
     */
    public Object eval(Environment env) {
        for (ASTree t: this)
            t.eval(env);
        return null;
    }
}