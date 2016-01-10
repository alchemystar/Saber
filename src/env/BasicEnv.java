package env;
import java.util.HashMap;

/**
 * 记录了符号的值
 */
public class BasicEnv implements Environment {
    protected HashMap<String,Object> values;
    public BasicEnv() { values = new HashMap<String,Object>(); }
    public void put(String name, Object value) { values.put(name, value); }
    public Object get(String name) { return values.get(name); }

    public void putNew(String name, Object value) {

    }

    public Environment where(String name) {
        return null;
    }

    public void setOuter(Environment e) {

    }
}
