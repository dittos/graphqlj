package org.sapzil.graphqlj.language;

/**
 * A representation of source input to GraphQL. The name is optional,
 * but is mostly useful for clients who store GraphQL documents in
 * souce files; for example, if the GraphQL input is in a file Foo.graphql,
 * it might be useful for name to be "Foo.graphql".
 */
public final class Source {
    private final String body;
    private final String name;

    public Source(String body) {
        this(body, "GraphQL");
    }

    public Source(String body, String name) {
        this.body = body;
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public String getName() {
        return name;
    }
}
