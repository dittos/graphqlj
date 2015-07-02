package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

import java.util.List;

public class ObjectValue extends Value {
    public ObjectValue(Location loc, List<ObjectField> fields) {
        super(loc);
    }
}
