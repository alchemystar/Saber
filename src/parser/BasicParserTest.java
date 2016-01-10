package parser;

import ast.ASTree;
import ast.NullStmnt;
import env.BasicEnv;
import env.Environment;
import env.NestedEnv;
import exception.ParseException;
import lexer.Lexer;
import lexer.Token;
import natives.Natives;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by alchemystar on 16/1/8.
 */
public class BasicParserTest {
    public static void main(String args[]) throws ParseException, FileNotFoundException {

        Reader reader = new FileReader("/Users/alchemystar/mycode/saber/src/parser/Program");
        Lexer lexer = new Lexer(reader);
        BasicParser bp = new ClassParser();
        Environment env = new Natives().environment(new NestedEnv());
        while (lexer.peek(0) != Token.EOF) {
            ASTree t = bp.parse(lexer);
            if (!(t instanceof NullStmnt)) {
                t.eval(env);
            }
        }
    }
}
