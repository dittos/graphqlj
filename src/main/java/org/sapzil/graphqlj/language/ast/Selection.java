package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public abstract class Selection extends Node {
    public Selection(Location loc) {
        super(loc);
    }
}
