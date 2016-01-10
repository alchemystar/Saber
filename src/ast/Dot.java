package ast;
import classinfo.ClassInfo;
import classinfo.SaberObject;
import env.Environment;
import env.NestedEnv;
import exception.SaberException;

import java.util.List;

public class Dot extends Postfix {
    public Dot(List<ASTree> c) { super(c); }
    public String name() { return ((ASTLeaf)child(0)).token().getText(); }
    public String toString() { return "." + name(); }

    public Object eval(Environment env, Object value) {
        String member = name();
        //如果是new ClassInfo操作的话,则创建对应的SaberObj以及环境
        if (value instanceof ClassInfo) {
            if ("new".equals(member)) {
                ClassInfo ci = (ClassInfo)value;
                NestedEnv e = new NestedEnv(ci.environment());
                SaberObject so = new SaberObject(e);
                e.putNew("this", so);
                initObject(ci, e);
                return so;
            }
        }
        else if (value instanceof SaberObject) {
            try {
                //此处可以返回normal member和func member
                //因为是按照闭包实现的
                return ((SaberObject)value).read(member);
            } catch (SaberObject.AccessException e) {}
        }
        throw new SaberException("bad member access: " + member, this);
    }
    protected void initObject(ClassInfo ci, Environment env) {
        //先子类的构造函数,再父类
        if (ci.superClass() != null)
            initObject(ci.superClass(), env);
        //这样做,表示每个对象都有一个自己的函数
        //类似闭包,事实上这些方法可以被共享
        ci.body().eval(env);
    }
}
