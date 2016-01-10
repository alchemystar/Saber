package lexer;

import exception.ParseException;

import java.io.Reader;
import java.io.StringReader;

/**
 * Created by alchemystar on 16/1/8.
 */
public class Lexer_Test {

    public static void main(String args[]) throws ParseException {
        String program = "a <= b+c*100";
        Reader reader = new StringReader(program);
        Lexer lexer = new Lexer(reader);
        for(Token t;(t=lexer.read())!=Token.EOF;){
            System.out.println("Lexer:   "+t.getText());
        }
    }
}
