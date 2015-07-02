package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public abstract class Value extends Node {
    public Value(Location loc) {
        super(loc);
    }
}
