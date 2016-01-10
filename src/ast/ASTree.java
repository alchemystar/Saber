package ast;
import env.Environment;
import exception.SaberException;

import java.util.Iterator;

public abstract class ASTree implements Iterable<ASTree> {
    public abstract ASTree child(int i);
    public abstract int numChildren();
    public abstract Iterator<ASTree> children();
    public abstract String location();
    public Iterator<ASTree> iterator() { return children(); }
    public Object eval(Environment env){throw new SaberException("can't env: abstract ASTree");}
}
