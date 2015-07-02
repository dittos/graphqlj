package org.sapzil.graphqlj.language;

public final class Lexer {
    private final Source source;
    private int prevPosition;

    public Lexer(Source source) {
        this.source = source;
        this.prevPosition = 0;
    }

    public Token nextToken() throws GraphQLLanguageException {
        return nextToken(prevPosition);
    }

    public Token nextToken(int resetPosition) throws GraphQLLanguageException {
        Token token = readToken(resetPosition);
        prevPosition = token.getEnd();
        return token;
    }

    /**
     * Helper function for constructing the Token object.
     */
    private Token makeToken(TokenKind kind, int start, int end) {
        return new Token(kind, start, end);
    }

    /**
     * Helper function for constructing the Token object.
     */
    private Token makeToken(TokenKind kind, int start, int end, String value) {
        return new Token(kind, start, end, value);
    }

    /**
     * Gets the next token from the source starting at the given position.
     *
     * This skips over whitespace and comments until it finds the next lexable
     * token, then lexes punctuators immediately or calls the appropriate helper
     * fucntion for more complicated tokens.
     */
    private Token readToken(int fromPosition) throws GraphQLLanguageException {
        String body = source.getBody();
        int bodyLength = body.length();

        int position = positionAfterWhitespace(body, fromPosition);

        if (position >= bodyLength) {
            return makeToken(TokenKind.EOF, position, position);
        }

        char code = body.charAt(position);

        switch (code) {
        // !
        case 33: return makeToken(TokenKind.BANG, position, position + 1);
        // $
        case 36: return makeToken(TokenKind.DOLLAR, position, position + 1);
        // (
        case 40: return makeToken(TokenKind.PAREN_L, position, position + 1);
        // )
        case 41: return makeToken(TokenKind.PAREN_R, position, position + 1);
        // .
        case 46:
            // TODO: bound check
            if (body.charAt(position + 1) == 46 &&
                    body.charAt(position + 2) == 46) {
                return makeToken(TokenKind.SPREAD, position, position + 3);
            }
            break;
        // :
        case 58: return makeToken(TokenKind.COLON, position, position + 1);
        // =
        case 61: return makeToken(TokenKind.EQUALS, position, position + 1);
        // @
        case 64: return makeToken(TokenKind.AT, position, position + 1);
        // [
        case 91: return makeToken(TokenKind.BRACKET_L, position, position + 1);
        // ]
        case 93: return makeToken(TokenKind.BRACKET_R, position, position + 1);
        // {
        case 123: return makeToken(TokenKind.BRACE_L, position, position + 1);
        // |
        case 124: return makeToken(TokenKind.PIPE, position, position + 1);
        // }
        case 125: return makeToken(TokenKind.BRACE_R, position, position + 1);
        // A-Z
        case 65: case 66: case 67: case 68: case 69: case 70: case 71: case 72:
        case 73: case 74: case 75: case 76: case 77: case 78: case 79: case 80:
        case 81: case 82: case 83: case 84: case 85: case 86: case 87: case 88:
        case 89: case 90:
            // _
        case 95:
            // a-z
        case 97: case 98: case 99: case 100: case 101: case 102: case 103: case 104:
        case 105: case 106: case 107: case 108: case 109: case 110: case 111:
        case 112: case 113: case 114: case 115: case 116: case 117: case 118:
        case 119: case 120: case 121: case 122:
            return readName(source, position);
        // -
        case 45:
            // 0-9
        case 48: case 49: case 50: case 51: case 52:
        case 53: case 54: case 55: case 56: case 57:
            return readNumber(source, position, code);
        // "
        case 34: return readString(source, position);
        }

        throw error(source, position, "Unexpected character \"" + code + "\"");
    }

    /**
     * Reads from body starting at startPosition until it finds a non-whitespace
     * or commented character, then returns the position of that character for
     * lexing.
     */
    private int positionAfterWhitespace(String body, int startPosition) {
        int bodyLength = body.length();
        int position = startPosition;
        while (position < bodyLength) {
            char code = body.charAt(position);
            // Skip whitespace
            if (code == 32 || // space
                    code == 44 || // comma
                    code == 160 || // '\xa0'
                    // TODO: is it okay to compare char with int > 2^8?
                    code == 0x2028 || // line separator
                    code == 0x2029 || // paragraph separator
                    code > 8 && code < 14 // whitespace
                    ) {
                ++position;
            // Skip comments
            } else if (code == 35) { // #
                ++position;
                while (position < bodyLength && (code = body.charAt(position)) != 0 &&
                        code != 10 && code != 13 && code != 0x2028 && code != 0x2029) {
                    ++position;
                }
            } else {
                break;
            }
        }
        return position;
    }

    /**
     * Reads a number token from the source file, either a float
     * or an int depending on whether a decimal point appears.
     *
     * Int:   -?(0|[1-9][0-9]*)
     * Float: -?(0|[1-9][0-9]*)\.[0-9]+(e-?[0-9]+)?
     */
    private Token readNumber(Source source, int start, char firstCode) throws GraphQLLanguageException {
        char code = firstCode;
        String body = source.getBody();
        int position = start;
        boolean isFloat = false;

        if (code == 45) { // -
            code = body.charAt(++position);
        }

        if (code == 48) { // 0
            code = body.charAt(++position);
        } else if (code >= 49 && code <= 57) { // 1 - 9
            do {
                code = body.charAt(++position);
            } while (code >= 48 && code <= 57); // 0 - 9
        } else {
            throw error(source, position, "Invalid number");
        }

        if (code == 46) { // .
            isFloat = true;

            code = body.charAt(++position);
            if (code >= 48 && code <= 57) { // 0 - 9
                do {
                    code = body.charAt(++position);
                } while (code >= 48 && code <= 57); // 0 - 9
            } else {
                throw error(source, position, "Invalid number");
            }

            if (code == 101) { // e
                code = body.charAt(++position);
                if (code == 45) { // -
                    code = body.charAt(++position);
                }
                if (code >= 48 && code <= 57) { // 0 - 9
                    do {
                        code = body.charAt(++position);
                    } while (code >= 48 && code <= 57); // 0 - 9
                } else {
                    throw error(source, position, "Invalid number");
                }
            }
        }

        return makeToken(
                isFloat ? TokenKind.FLOAT : TokenKind.INT,
                start,
                position,
                body.substring(start, position)
        );
    }

    /**
     * Reads a string token from the source file.
     *
     * "([^"\\\u000A\u000D\u2028\u2029]|(\\(u[0-9a-fA-F]{4}|["\\/bfnrt])))*"
     */
    private Token readString(Source source, int start) throws GraphQLLanguageException {
        String body = source.getBody();
        int position = start + 1;
        int chunkStart = position;
        char code = 0;
        StringBuilder value = new StringBuilder();

        while (
                position < body.length() &&
                        (code = body.charAt(position)) != 0 &&
                        code != 34 &&
                        code != 10 && code != 13 && code != 0x2028 && code != 0x2029
                ) {
            ++position;
            if (code == 92) { // \
                value.append(body, chunkStart, position - 1);
                code = body.charAt(position);
                switch (code) {
                case 34: value.append('"'); break;
                case 47: value.append('/'); break;
                case 92: value.append('\\'); break;
                case 98: value.append('\b'); break;
                case 102: value.append('\f'); break;
                case 110: value.append('\n'); break;
                case 114: value.append('\r'); break;
                case 116: value.append('\t'); break;
                case 117:
                    int charCode = uniCharCode(
                            body.charAt(position + 1),
                            body.charAt(position + 2),
                            body.charAt(position + 3),
                            body.charAt(position + 4)
                    );
                    if (charCode < 0) {
                        throw error(source, position, "Bad character escape sequence");
                    }
                    value.appendCodePoint(charCode);
                    position += 4;
                    break;
                default:
                    throw error(source, position, "Bad character escape sequence");
                }
                ++position;
                chunkStart = position;
            }
        }

        if (code != 34) {
            throw error(source, position, "Unterminated string");
        }

        value.append(body, chunkStart, position);
        return makeToken(TokenKind.STRING, start, position + 1, value.toString());
    }

    /**
     * Converts four hexidecimal chars to the integer that the
     * string represents. For example, uniCharCode('0','0','0','f')
     * will return 15, and uniCharCode('0','0','f','f') returns 255.
     *
     * Returns a negative number on error, if a char was invalid.
     *
     * This is implemented by noting that char2hex() returns -1 on error,
     * which means the result of ORing the char2hex() will also be negative.
     */
    private int uniCharCode(char a, char b, char c, char d) {
        return char2hex(a) << 12 | char2hex(b) << 8 | char2hex(c) << 4 | char2hex(d);
    }

    /**
     * Converts a hex character to its integer value.
     * '0' becomes 0, '9' becomes 9
     * 'A' becomes 10, 'F' becomes 15
     * 'a' becomes 10, 'f' becomes 15
     *
     * Returns -1 on error.
     */
    private int char2hex(char a) {
        return (
                a >= 48 && a <= 57 ? a - 48 : // 0-9
                        a >= 65 && a <= 70 ? a - 55 : // A-F
                                a >= 97 && a <= 102 ? a - 87 : // a-f
                                        -1
        );
    }

    /**
     * Reads an alphanumeric + underscore name from the source.
     *
     * [_A-Za-z][_0-9A-Za-z]*
     */
    private Token readName(Source source, int position) {
        String body = source.getBody();
        int bodyLength = body.length();
        int end = position + 1;
        int code;
        while (
                end != bodyLength &&
                        // TODO: safe charAt
                        (code = body.charAt(end)) != 0 &&
                        (
                                code == 95 || // _
                                        code >= 48 && code <= 57 || // 0-9
                                        code >= 65 && code <= 90 || // A-Z
                                        code >= 97 && code <= 122 // a-z
                        )
                ) {
            ++end;
        }
        return makeToken(
                TokenKind.NAME,
                position,
                end,
                body.substring(position, end)
        );
    }

    private GraphQLLanguageException error(Source source, int position, String message) {
        return new GraphQLLanguageException(source, position, message);
    }
}
