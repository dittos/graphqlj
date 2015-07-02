package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class ObjectField extends Node {
    public ObjectField(Location loc, Name name, Value value) {
        super(loc);
    }
}
