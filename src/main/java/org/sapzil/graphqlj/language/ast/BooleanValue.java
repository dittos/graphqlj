package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class BooleanValue extends Value {
    public BooleanValue(Location loc, boolean value) {
        super(loc);
    }
}
