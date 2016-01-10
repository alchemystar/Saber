package lexer;

/**
 * Created by alchemystar on 16/1/8.
 */

/**
 * 分割成若干单词,词素
 */
public  abstract class Token {
    //文件末尾
    public static final Token EOF = new Token(-1){};
    //一行末尾
    public static final String EOL = "\\n";
    //词素所在行
    private int lineNumber;

    protected Token(int line){
        lineNumber = line;
    }

    public int getLineNumber(){return lineNumber;}

    public boolean isIdentifier() {return false;}

    public boolean isNumber() {return false;}

    public boolean isString() {return false;}

    public int getNumber() {throw new RuntimeException("not number token");}

    public String getText() {return "";}

}
