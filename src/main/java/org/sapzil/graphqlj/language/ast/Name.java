package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class Name extends Node {
    private final String value;

    public Name(Location loc, String value) {
        super(loc);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
