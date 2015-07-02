package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class NonNullType extends Type {
    public NonNullType(Location loc, Type type) {
        super(loc);
    }
}
