package parser;

import ast.*;
import lexer.Token;

import static parser.Parser.rule;


public class ClassParser extends FuncParser {
    Parser member = rule().or(def, simple);
    Parser class_body = rule(ClassBody.class).sep("{").option(member)
                            .repeat(rule().sep(";", Token.EOL).option(member))
                            .sep("}");
    Parser defclass = rule(ClassStmnt.class).sep("class").identifier(reserved)
                          .option(rule().sep("extends").identifier(reserved))
                          .ast(class_body);
    Parser elements = rule(ArrayLiteral.class)
            .ast(expr).repeat(rule().sep(",").ast(expr));
    public ClassParser() {
        postfix.insertChoice(rule(Dot.class).sep(".").identifier(reserved));
        program.insertChoice(defclass);
        reserved.add("]");
        primary.insertChoice(rule().sep("[").maybe(elements).sep("]"));
        postfix.insertChoice(rule(ArrayRef.class).sep("[").ast(expr).sep("]"));
    }
}
