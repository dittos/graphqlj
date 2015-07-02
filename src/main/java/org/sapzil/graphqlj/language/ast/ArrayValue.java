package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

import java.util.List;

public class ArrayValue extends Value {
    public ArrayValue(Location loc, List<Value> items) {
        super(loc);
    }
}
