package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class Variable extends Value {
    public Variable(Location loc, Name name) {
        super(loc);
    }
}
