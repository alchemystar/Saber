package ast;
import env.Environment;
import exception.SaberException;
import natives.NativeFunction;

import java.util.List;

public class Arguments extends Postfix {
    public Arguments(List<ASTree> c) { super(c); }
    public int size() { return numChildren(); }

    public Object eval(Environment callerEnv, Object value) {
        if (!((value instanceof Function) || (value instanceof NativeFunction)))
            throw new SaberException("bad function", this);
        //java本地方法的判断
        if(value instanceof NativeFunction){
            NativeFunction func = (NativeFunction)value;
            int nparams = func.numOfParameters();
            if (size() != nparams)
                throw new SaberException("bad number of arguments", this);
            Object[] args = new Object[nparams];
            int num = 0;
            for (ASTree a: this) {
                args[num++] = a.eval(callerEnv);
            }
            return func.invoke(args, this);
        }else {
            Function func = (Function) value;
            ParameterList params = func.parameters();
            if (size() != params.size())
                throw new SaberException("bad number of arguments", this);
            Environment newEnv = func.makeEnv();
            int num = 0;
            for (ASTree a : this) {
                //计算每个param,并放入相应的环境中
                params.eval(newEnv, num++, a.eval(callerEnv));
            }
            return func.body().eval(newEnv);
        }
    }
}
