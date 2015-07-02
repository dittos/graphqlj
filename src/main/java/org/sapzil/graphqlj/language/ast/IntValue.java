package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class IntValue extends Value {
    public IntValue(Location loc, String value) {
        super(loc);
    }
}
