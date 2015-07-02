package org.sapzil.graphqlj.language;

import org.sapzil.graphqlj.language.ast.Argument;
import org.sapzil.graphqlj.language.ast.ArrayValue;
import org.sapzil.graphqlj.language.ast.BaseType;
import org.sapzil.graphqlj.language.ast.BooleanValue;
import org.sapzil.graphqlj.language.ast.Directive;
import org.sapzil.graphqlj.language.ast.Document;
import org.sapzil.graphqlj.language.ast.EnumValue;
import org.sapzil.graphqlj.language.ast.Field;
import org.sapzil.graphqlj.language.ast.FloatValue;
import org.sapzil.graphqlj.language.ast.Fragment;
import org.sapzil.graphqlj.language.ast.FragmentDefinition;
import org.sapzil.graphqlj.language.ast.FragmentSpread;
import org.sapzil.graphqlj.language.ast.InlineFragment;
import org.sapzil.graphqlj.language.ast.IntValue;
import org.sapzil.graphqlj.language.ast.ListType;
import org.sapzil.graphqlj.language.ast.Name;
import org.sapzil.graphqlj.language.ast.NonNullType;
import org.sapzil.graphqlj.language.ast.ObjectField;
import org.sapzil.graphqlj.language.ast.ObjectValue;
import org.sapzil.graphqlj.language.ast.OperationDefinition;
import org.sapzil.graphqlj.language.ast.Selection;
import org.sapzil.graphqlj.language.ast.SelectionSet;
import org.sapzil.graphqlj.language.ast.StringValue;
import org.sapzil.graphqlj.language.ast.Type;
import org.sapzil.graphqlj.language.ast.Value;
import org.sapzil.graphqlj.language.ast.Variable;
import org.sapzil.graphqlj.language.ast.VariableDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class Parser {
    public static Document parse(Source source, ParseOptions options) throws GraphQLLanguageException {
        Parser parser = new Parser(source, options);
        return parser.parseDocument();
    }

    private final Lexer lexer;
    private final Source source;
    private final ParseOptions options;
    private int prevEnd;
    private Token token;

    public Parser(Source source, ParseOptions options) throws GraphQLLanguageException {
        this.source = source;
        this.options = options;
        this.lexer = new Lexer(source);
        this.token = lexer.nextToken();
    }

    private Location loc(int start) {
        if (options.noLocation()) {
            return null;
        }
        if (options.noSource()) {
            return new Location(start, prevEnd);
        }
        return new Location(start, prevEnd, source);
    }

    /**
     * Moves the internal parser object to the next lexed token.
     */
    private void advance() throws GraphQLLanguageException {
        int prevEnd = token.getEnd();
        this.prevEnd = prevEnd;
        token = lexer.nextToken(prevEnd);
    }

    /**
     * Determines if the next token is of a given kind
     */
    private boolean peek(TokenKind kind) {
        return token.getKind() == kind;
    }

    /**
     * If the next token is of the given kind, return true after advancing
     * the parser. Otherwise, do not change the parser state and return false.
     */
    private boolean skip(TokenKind kind) throws GraphQLLanguageException {
        boolean match = token.getKind() == kind;
        if (match) {
            advance();
        }
        return match;
    }

    /**
     * If the next token is of the given kind, return that token after advancing
     * the parser. Otherwise, do not change the parser state and return false.
     */
    private Token expect(TokenKind kind) throws GraphQLLanguageException {
        Token token = this.token;
        if (token.getKind() == kind) {
            advance();
            return token;
        }
        throw new GraphQLLanguageException(
                source,
                token.getStart(),
                "Expected " + kind.getDesc() + ", found " + token.getDesc()
        );
    }

    /**
     * If the next token is a keyword with the given value, return that token after
     * advancing the parser. Otherwise, do not change the parser state and return
     * false.
     */
    private Token expectKeyword(String value) throws GraphQLLanguageException {
        Token token = this.token;
        if (token.getKind() == TokenKind.NAME && value.equals(token.getValue())) {
            advance();
            return token;
        }
        throw new GraphQLLanguageException(
                source,
                token.getStart(),
                "Expected \"" + value + "\", found " + token.getDesc()
        );
    }

    /**
     * Helper function for creating an error when an unexpected lexed token
     * is encountered.
     */
    private GraphQLLanguageException unexpected() {
        return unexpected(token);
    }

    /**
     * Helper function for creating an error when an unexpected lexed token
     * is encountered.
     */
    private GraphQLLanguageException unexpected(Token atToken) {
        Token token = atToken;
        return new GraphQLLanguageException(
                source,
                token.getStart(),
                "Unexpected " + token.getDesc()
        );
    }

    /**
     * Returns a possibly empty list of parse nodes, determined by
     * the parseFn. This list begins with a lex token of openKind
     * and ends with a lex token of closeKind. Advances the parser
     * to the next lex token after the closing token.
     */
    private <T> List<T> any(TokenKind openKind, Callable<T> parseFn, TokenKind closeKind) throws GraphQLLanguageException {
        expect(openKind);
        List<T> nodes = new ArrayList<>();
        while (!skip(closeKind)) {
            try {
                nodes.add(parseFn.call());
            } catch (GraphQLLanguageException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return nodes;
    }

    /**
     * Returns a non-empty list of parse nodes, determined by
     * the parseFn. This list begins with a lex token of openKind
     * and ends with a lex token of closeKind. Advances the parser
     * to the next lex token after the closing token.
     */
    private <T> List<T> many(TokenKind openKind, Callable<T> parseFn, TokenKind closeKind) throws GraphQLLanguageException {
        expect(openKind);
        List<T> nodes = new ArrayList<>();
        try {
            nodes.add(parseFn.call());
            while (!skip(closeKind)) {
                nodes.add(parseFn.call());
            }
        } catch (GraphQLLanguageException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return nodes;
    }

    /**
     * Converts a name lex token into a name parse node.
     */
    private Name parseName() throws GraphQLLanguageException {
        Token token = expect(TokenKind.NAME);
        return new Name(loc(token.getStart()), token.getValue());
    }


    // Implements the parsing rules in the Document section.

    private Document parseDocument() throws GraphQLLanguageException {
        int start = token.getStart();
        List definitions = new ArrayList<>();
        do {
            if (peek(TokenKind.BRACE_L)) {
                definitions.add(parseOperationDefinition());
            } else if (peek(TokenKind.NAME)) {
                if ("query".equals(token.getValue()) || "mutation".equals(token.getValue())) {
                    definitions.add(parseOperationDefinition());
                } else if ("fragment".equals(token.getValue())) {
                    definitions.add(parseFragmentDefinition());
                } else {
                    throw unexpected();
                }
            } else {
                throw unexpected();
            }
        } while (!skip(TokenKind.EOF));
        return new Document(definitions, loc(start));
    }


    // Implements the parsing rules in the Operations section.

    private OperationDefinition parseOperationDefinition() throws GraphQLLanguageException {
        int start = token.getStart();
        if (peek(TokenKind.BRACE_L)) {
            return new OperationDefinition(loc(start), "query", null, null, Collections.emptyList(), parseSelectionSet());
        }
        Token operationToken = expect(TokenKind.NAME);
        String operation = operationToken.getValue();
        return new OperationDefinition(loc(start), operation, parseName(), parseVariableDefinitions(), parseDirectives(), parseSelectionSet());
    }

    private List<VariableDefinition> parseVariableDefinitions() throws GraphQLLanguageException {
        return peek(TokenKind.PAREN_L) ?
                many(
                        TokenKind.PAREN_L,
                        new Callable<VariableDefinition>() {
                            @Override public VariableDefinition call() throws Exception {
                                return parseVariableDefinition();
                            }
                        },
                        TokenKind.PAREN_R
                ) :
            Collections.emptyList();
    }

    private VariableDefinition parseVariableDefinition() throws GraphQLLanguageException {
        int start = token.getStart();
        Variable variable = parseVariable();
        expect(TokenKind.COLON);
        Type type = parseType();
        Value defaultValue = skip(TokenKind.EQUALS) ? parseValue(true) : null;
        return new VariableDefinition(loc(start), variable, type, defaultValue);
    }

    private Variable parseVariable() throws GraphQLLanguageException {
        int start = token.getStart();
        expect(TokenKind.DOLLAR);
        return new Variable(loc(start), parseName());
    }

    private SelectionSet parseSelectionSet() throws GraphQLLanguageException {
        int start = token.getStart();
        return new SelectionSet(loc(start), many(TokenKind.BRACE_L, new Callable<Selection>() {
            @Override public Selection call() throws Exception {
                return parseSelection();
            }
        }, TokenKind.BRACE_R));
    }

    private Selection parseSelection() throws GraphQLLanguageException {
        return peek(TokenKind.SPREAD) ?
                parseFragment() :
                parseField();
    }

    /**
     * Corresponds to both Field and Alias in the spec
     */
    private Field parseField() throws GraphQLLanguageException {
        int start = token.getStart();

        Name nameOrAlias = parseName();
        Name alias;
        Name name;
        if (skip(TokenKind.COLON)) {
            alias = nameOrAlias;
            name = parseName();
        } else {
            alias = null;
            name = nameOrAlias;
        }

        return new Field(loc(start), alias, name, parseArguments(), parseDirectives(),
                peek(TokenKind.BRACE_L) ? parseSelectionSet() : null);
    }

    private List<Argument> parseArguments() throws GraphQLLanguageException {
        return peek(TokenKind.PAREN_L) ?
                many(TokenKind.PAREN_L, new Callable<Argument>() {
                    @Override public Argument call() throws Exception {
                        return parseArgument();
                    }
                }, TokenKind.PAREN_R) :
            Collections.emptyList();
    }

    private Argument parseArgument() throws Exception {
        int start = token.getStart();
        Name name = parseName();
        expect(TokenKind.COLON);
        Value value = parseValue(false);
        return new Argument(loc(start), name, value);
    }


    // Implements the parsing rules in the Fragments section.

    /**
     * Corresponds to both FragmentSpread and InlineFragment in the spec
     */
    private Fragment parseFragment() throws GraphQLLanguageException {
        int start = token.getStart();
        expect(TokenKind.SPREAD);
        if ("on".equals(token.getValue())) {
            advance();
            return new InlineFragment(loc(start), parseName(), parseDirectives(), parseSelectionSet());
        }
        return new FragmentSpread(loc(start), parseName(), parseDirectives());
    }

    private FragmentDefinition parseFragmentDefinition() throws GraphQLLanguageException {
        int start = token.getStart();
        expectKeyword("fragment");
        Name name = parseName();
        expectKeyword("on");
        Name typeCondition = parseName();
        List<Directive> directives = parseDirectives();
        SelectionSet selectionSet = parseSelectionSet();
        return new FragmentDefinition(loc(start), name, typeCondition, directives, selectionSet);
    }


    // Implements the parsing rules in the Values section.

    private Value parseVariableValue() throws GraphQLLanguageException {
        return parseValue(false);
    }

    private Value parseConstValue() throws GraphQLLanguageException {
        return parseValue(true);
    }

    private Value parseValue(boolean isConst) throws GraphQLLanguageException {
        Token token = this.token;
        switch (token.getKind()) {
        case BRACKET_L:
            return parseArray(isConst);
        case BRACE_L:
            return parseObject(isConst);
        case INT:
            advance();
            return new IntValue(loc(token.getStart()), token.getValue());
        case FLOAT:
            advance();
            return new FloatValue(loc(token.getStart()), token.getValue());
        case STRING:
            advance();
            return new StringValue(loc(token.getStart()), token.getValue());
        case NAME:
            advance();
            switch (token.getValue()) {
            case "true":
            case "false":
                return new BooleanValue(loc(token.getStart()), "true".equals(token.getValue()));
            }
            return new EnumValue(loc(token.getStart()), token.getValue());
        case DOLLAR:
            if (!isConst) {
                return parseVariable();
            }
            break;
        }
        throw unexpected();
    }

    private ArrayValue parseArray(boolean isConst) throws GraphQLLanguageException {
        int start = token.getStart();
        Callable<Value> item = isConst ? new Callable<Value>() {
            @Override public Value call() throws Exception {
                return parseConstValue();
            }
        } : new Callable<Value>() {
            @Override public Value call() throws Exception {
                return parseVariableValue();
            }
        };
        return new ArrayValue(loc(start), any(TokenKind.BRACKET_L, item, TokenKind.BRACKET_R));
    }

    private ObjectValue parseObject(boolean isConst) throws GraphQLLanguageException {
        int start = token.getStart();
        expect(TokenKind.BRACE_L);
        Map<String, Boolean> fieldNames = new HashMap<>();
        List<ObjectField> fields = new ArrayList<>();
        while (!skip(TokenKind.BRACE_R)) {
            fields.add(parseObjectField(isConst, fieldNames));
        }
        return new ObjectValue(loc(start), fields);
    }

    private ObjectField parseObjectField(boolean isConst, Map<String, Boolean> fieldNames) throws GraphQLLanguageException {
        int start = token.getStart();
        Name name = parseName();
        if (fieldNames.containsKey(name.getValue())) {
            throw new GraphQLLanguageException(source, start, "Duplicate input object field " + name.getValue() + ".");
        }
        fieldNames.put(name.getValue(), true);
        expect(TokenKind.COLON);
        Value value = parseValue(isConst);
        return new ObjectField(loc(start), name, value);
    }


    // Implements the parsing rules in the Directives section.

    private List<Directive> parseDirectives() throws GraphQLLanguageException {
        List<Directive> directives = new ArrayList<>();
        while (peek(TokenKind.AT)) {
            directives.add(parseDirective());
        }
        return directives;
    }

    private Directive parseDirective() throws GraphQLLanguageException {
        int start = token.getStart();
        expect(TokenKind.AT);
        return new Directive(loc(start), parseName(), skip(TokenKind.COLON) ? parseValue(false) : null);
    }
    

    // Implements the parsing rules in the Types section.

    /**
     * Handles the Type: TypeName, ListType, and NonNullType parsing rules.
     */
    private Type parseType() throws GraphQLLanguageException {
        int start = token.getStart();
        Type type;
        if (skip(TokenKind.BRACKET_L)) {
            type = parseType();
            expect(TokenKind.BRACKET_R);
            type = new ListType(loc(start), type);
        } else {
            type = new BaseType(loc(start), parseName());
        }
        if (skip(TokenKind.BANG)) {
            return new NonNullType(loc(start), type);
        }
        return type;
    }
}
