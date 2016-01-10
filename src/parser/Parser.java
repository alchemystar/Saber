package parser;

import ast.ASTLeaf;
import ast.ASTList;
import ast.ASTree;
import exception.ParseException;
import lexer.Lexer;
import lexer.Token;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Parser库的原理其实就是:
 * 给Program添加多种模式,例如Program.skip("(").ast(expr).skip(")");
 * 事实上就是先对"("模式进行Parse,再对expr模式进行parse,再对")"模式进行Parse
 * 同时Program Parse前先生成ASTree节点,底下模式生成的ASTree节点链接上去,生成
 * 一个Parse Program的完整的AST树
 */
public class Parser {
    protected static abstract class Element {
        protected abstract void parse(Lexer lexer, List<ASTree> res)
            throws ParseException;
        protected abstract boolean match(Lexer lexer) throws ParseException;
    }

    protected static class Tree extends Element {
        protected Parser parser;
        protected Tree(Parser p) { parser = p; }
        protected void parse(Lexer lexer, List<ASTree> res)
            throws ParseException
        {
            res.add(parser.parse(lexer));
        }
        protected boolean match(Lexer lexer) throws ParseException { 
            return parser.match(lexer);
        }
    }
    //BNF范式的『|』形式,必须满足第一个词素即可判断分支ß
    protected static class OrTree extends Element {
        protected Parser[] parsers;
        protected OrTree(Parser[] p) { parsers = p; }
        //res为其上层节点
        protected void parse(Lexer lexer, List<ASTree> res)
            throws ParseException
        {
            Parser p = choose(lexer);
            if (p == null)
                throw new ParseException(lexer.peek(0));
            else
                res.add(p.parse(lexer));
        }
        //此处的match,只要满足此模式中的任意一规则就能成功,
        //是为了处理or(or()...)的形式
        protected boolean match(Lexer lexer) throws ParseException {
            return choose(lexer) != null;
        }
        protected Parser choose(Lexer lexer) throws ParseException {
            //循环放在所有orTree中的Parser,找到第一个匹配的那一项
            for (Parser p: parsers)
                if (p.match(lexer))
                    return p;

            return null;
        }
        protected void insert(Parser p) {
            Parser[] newParsers = new Parser[parsers.length + 1];
            newParsers[0] = p;
            System.arraycopy(parsers, 0, newParsers, 1, parsers.length);
            parsers = newParsers;
        }
    }

    protected static class Repeat extends Element {
        protected Parser parser;
        protected boolean onlyOnce;
        protected Repeat(Parser p, boolean once) { parser = p; onlyOnce = once; }
        protected void parse(Lexer lexer, List<ASTree> res)
            throws ParseException
        {
            //如果Repeat中的Parser匹配,则继续匹配,知道匹配不到为止
            //或者如果只设置了单次匹配,直接跳出
            //匹配出来的ASTree放到res里面,形成更大的树结构
            while (parser.match(lexer)) {
                ASTree t = parser.parse(lexer);
                if (t.getClass() != ASTList.class || t.numChildren() > 0)
                    res.add(t);
                if (onlyOnce)
                    break;
            }
        }
        protected boolean match(Lexer lexer) throws ParseException {
            return parser.match(lexer);
        }
    }

    protected static abstract class AToken extends Element {
        protected Factory factory;
        protected AToken(Class<? extends ASTLeaf> type) {
            if (type == null)
                type = ASTLeaf.class;
            factory = Factory.get(type, Token.class);
        }
        protected void parse(Lexer lexer, List<ASTree> res)
            throws ParseException
        {
            Token t = lexer.read();
            if (test(t)) {
                ASTree leaf = factory.make(t);
                res.add(leaf);
            }
            else
                throw new ParseException(t);
        }
        protected boolean match(Lexer lexer) throws ParseException {
            return test(lexer.peek(0));
        }
        protected abstract boolean test(Token t); 
    }

    //Id模式
    protected static class IdToken extends AToken {
        HashSet<String> reserved;
        protected IdToken(Class<? extends ASTLeaf> type, HashSet<String> r) {
            super(type);
            reserved = r != null ? r : new HashSet<String>();
        }
        protected boolean test(Token t) {
            return t.isIdentifier() && !reserved.contains(t.getText());
        }
    }

    //数字模式
    protected static class NumToken extends AToken {
        protected NumToken(Class<? extends ASTLeaf> type) { super(type); }
        protected boolean test(Token t) { return t.isNumber(); }
    }

    //字符串模式
    protected static class StrToken extends AToken {
        protected StrToken(Class<? extends ASTLeaf> type) {
            super(type);
        }

        protected boolean test(Token t) {
            return t.isString();
        }
    }

    protected static class Leaf extends Element {
        protected String[] tokens;
        protected Leaf(String[] pat) { tokens = pat; }
        protected void parse(Lexer lexer, List<ASTree> res)
            throws ParseException
        {
            Token t = lexer.read();
            //此处的判断是为了,skip("{")等的判断
            if (t.isIdentifier())
                for (String token: tokens)
                    if (token.equals(t.getText())) {
                        find(res, t);
                        return;
                    }

            if (tokens.length > 0)
                throw new ParseException(tokens[0] + " expected.", t);
            else
                throw new ParseException(t);
        }
        protected void find(List<ASTree> res, Token t) {
            res.add(new ASTLeaf(t));
        }
        protected boolean match(Lexer lexer) throws ParseException {
            Token t = lexer.peek(0);
            if (t.isIdentifier())
                for (String token: tokens)
                    if (token.equals(t.getText()))
                        return true;

            return false;
        }
    }

    protected static class Skip extends Leaf {
        protected Skip(String[] t) { super(t); }
        protected void find(List<ASTree> res, Token t) {}
    }

    public static class Precedence {
        int value;
        boolean leftAssoc; // left associative
        public Precedence(int v, boolean a) {
            value = v; leftAssoc = a;
        }
    }

    public static class Operators extends HashMap<String,Precedence> {
        public static boolean LEFT = true;
        public static boolean RIGHT = false;
        public void add(String name, int prec, boolean leftAssoc) {
            put(name, new Precedence(prec, leftAssoc));
        }
    }

    //用了运算符优先的Expr表达式的Parse
    protected static class Expr extends Element {
        protected Factory factory;
        protected Operators ops;
        protected Parser factor;
        protected Expr(Class<? extends ASTree> clazz, Parser exp,
                       Operators map)
        {
            factory = Factory.getForASTList(clazz);
            ops = map;
            factor = exp;
        }
        public void parse(Lexer lexer, List<ASTree> res) throws ParseException {
            ASTree right = factor.parse(lexer);
            Precedence prec;
            while ((prec = nextOperator(lexer)) != null)
                right = doShift(lexer, right, prec.value);

            res.add(right);
        }
        private ASTree doShift(Lexer lexer, ASTree left, int prec)
            throws ParseException
        {
            ArrayList<ASTree> list = new ArrayList<ASTree>();
            list.add(left);
            list.add(new ASTLeaf(lexer.read()));
            ASTree right = factor.parse(lexer);
            Precedence next;
            while ((next = nextOperator(lexer)) != null
                   && rightIsExpr(prec, next))
                right = doShift(lexer, right, next.value);

            list.add(right);
            return factory.make(list);
        }
        private Precedence nextOperator(Lexer lexer) throws ParseException {
            Token t = lexer.peek(0);
            if (t.isIdentifier())
                return ops.get(t.getText());
            else
                return null;
        }
        private static boolean rightIsExpr(int prec, Precedence nextPrec) {
            if (nextPrec.leftAssoc)
                return prec < nextPrec.value;
            else
                return prec <= nextPrec.value;
        }
        protected boolean match(Lexer lexer) throws ParseException {
            return factor.match(lexer);
        }
    }

    public static final String factoryName = "create";
    protected static abstract class Factory {
        protected abstract ASTree make0(Object arg) throws Exception;
        protected ASTree make(Object arg) {
            try {
                return make0(arg);
            } catch (IllegalArgumentException e1) {
                throw e1;
            } catch (Exception e2) {
                throw new RuntimeException(e2); // this compiler is broken.
            }
        }
        protected static Factory getForASTList(Class<? extends ASTree> clazz) {
            Factory f = get(clazz, List.class);
            if (f == null)
                f = new Factory() {
                    protected ASTree make0(Object arg) throws Exception {
                        List<ASTree> results = (List<ASTree>)arg;
                        //此步是优化,如果child只有一个,直接返回此child
                        if (results.size() == 1)
                            return results.get(0);
                        else
                            return new ASTList(results);
                    }
                };
            return f;
        }
        protected static Factory get(Class<? extends ASTree> clazz,
                                     Class<?> argType)
        {
            if (clazz == null)
                return null;
            try {
                final Method m = clazz.getMethod(factoryName,
                                                 new Class<?>[] { argType });
                return new Factory() {
                    protected ASTree make0(Object arg) throws Exception {
                        return (ASTree)m.invoke(null, arg);
                    }
                };
            } catch (NoSuchMethodException e) {}
            try {
                final Constructor<? extends ASTree> c
                    = clazz.getConstructor(argType);
                return new Factory() {
                    protected ASTree make0(Object arg) throws Exception {
                        return c.newInstance(arg);
                    }
                };
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected List<Element> elements;
    protected Factory factory;

    public Parser(Class<? extends ASTree> clazz) {
        reset(clazz);
    }
    protected Parser(Parser p) {
        elements = p.elements;
        factory = p.factory;
    }

    /**
     * 此函数为起始parse,按照其中elements中的成分来Parse
     * 同时生成ASTree根节点,然后elements的Parser出的结果会加入此根节点
     * 从而形成一个完整的语法树
     * @param lexer
     * @return
     * @throws ParseException
     */
    public ASTree parse(Lexer lexer) throws ParseException {
        ArrayList<ASTree> results = new ArrayList<ASTree>();
        for (Element e: elements)
            e.parse(lexer, results);
        //此处的factory,不同模式映射为不同的类,并被make出来
        return factory.make(results);
    }
    protected boolean match(Lexer lexer) throws ParseException {
        if (elements.size() == 0)
            return true;
        else {
            //此处get(0)来match,表示只向前看一个字符
            Element e = elements.get(0);
            return e.match(lexer);
        }
    }
    public static Parser rule() { return rule(null); }
    //此处的Rule返回的Parser,将在匹配与此对应的模式时候,返回对应的Class类
    //这些类重载了eval等方法,用于程序的执行等
    public static Parser rule(Class<? extends ASTree> clazz) {
        return new Parser(clazz);
    }
    public Parser reset() {
        elements = new ArrayList<Element>();
        return this;
    }
    public Parser reset(Class<? extends ASTree> clazz) {
        elements = new ArrayList<Element>();
        factory = Factory.getForASTList(clazz);
        return this;
    }
    public Parser number() {
        return number(null);
    }
    public Parser number(Class<? extends ASTLeaf> clazz) {
        elements.add(new NumToken(clazz));
        return this;
    }
    public Parser identifier(HashSet<String> reserved) {
        return identifier(null, reserved);
    }
    public Parser identifier(Class<? extends ASTLeaf> clazz,
                             HashSet<String> reserved)
    {
        elements.add(new IdToken(clazz, reserved));
        return this;
    }
    public Parser string() {
        return string(null);
    }
    public Parser string(Class<? extends ASTLeaf> clazz) {
        elements.add(new StrToken(clazz));
        return this;
    }
    public Parser token(String... pat) {
        elements.add(new Leaf(pat));
        return this;
    }
    public Parser sep(String... pat) {
        elements.add(new Skip(pat));
        return this;
    }
    public Parser ast(Parser p) {
        elements.add(new Tree(p));
        return this;
    }
    public Parser or(Parser... p) {
        elements.add(new OrTree(p));
        return this;
    }
    public Parser maybe(Parser p) {
        Parser p2 = new Parser(p);
        p2.reset();
        elements.add(new OrTree(new Parser[] { p, p2 }));
        return this;
    }
    public Parser option(Parser p) {
        elements.add(new Repeat(p, true));
        return this;
    }
    public Parser repeat(Parser p) {
        elements.add(new Repeat(p, false));
        return this;
    }
    public Parser expression(Parser subexp, Operators operators) {
        elements.add(new Expr(null, subexp, operators));
        return this;
    }
    public Parser expression(Class<? extends ASTree> clazz, Parser subexp,
                             Operators operators) {
        elements.add(new Expr(clazz, subexp, operators));
        return this;
    }
    public Parser insertChoice(Parser p) {
        Element e = elements.get(0);
        if (e instanceof OrTree)
            ((OrTree)e).insert(p);
        else {
            Parser otherwise = new Parser(this);
            reset(null);
            or(p, otherwise);
        }
        return this;
    }
}
