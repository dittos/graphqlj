package org.sapzil.graphqlj.language;

public class GraphQLLanguageException extends Exception {
    public GraphQLLanguageException(Source source, int position, String message) {
        // TODO: better error reporting
        super(message);
    }
}
