package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class ListType extends Type {
    public ListType(Location loc, Type type) {
        super(loc);
    }
}
