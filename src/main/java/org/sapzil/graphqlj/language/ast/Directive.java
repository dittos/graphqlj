package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class Directive extends Node {
    public Directive(Location loc, Name name, Value value) {
        super(loc);
    }
}
