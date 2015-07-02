package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class Argument extends Node {
    public Argument(Location loc, Name name, Value value) {
        super(loc);
    }
}
