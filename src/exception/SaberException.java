package exception;

import ast.ASTree;

/**
 * Created by alchemystar on 16/1/8.
 */
public class SaberException extends RuntimeException {
    public SaberException(String m) {super(m);}
    public SaberException(String m, ASTree t){
        super(m+" "+t.location());
    }
}
