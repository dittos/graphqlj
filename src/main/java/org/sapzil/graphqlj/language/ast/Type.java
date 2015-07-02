package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public abstract class Type extends Node {
    public Type(Location loc) {
        super(loc);
    }
}
