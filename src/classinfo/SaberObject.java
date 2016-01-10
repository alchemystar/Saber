package classinfo;

import env.Environment;

/**
 * Created by alchemystar on 16/1/9.
 */

/**
 * 类java的obj,其中存储了此class的环境
 */
public class SaberObject {
    public static class AccessException extends Exception {}
    protected Environment env;
    public SaberObject(Environment e) { env = e; }
    @Override public String toString() { return "<object:" + hashCode() + ">"; }
    public Object read(String member) throws AccessException {
        return getEnv(member).get(member);
    }
    public void write(String member, Object value) throws AccessException {
        getEnv(member).putNew(member, value);
    }
    protected Environment getEnv(String member) throws AccessException {
        Environment e = env.where(member);
        if (e != null && e == env)
            return e;
        else
            throw new AccessException();
    }
}
