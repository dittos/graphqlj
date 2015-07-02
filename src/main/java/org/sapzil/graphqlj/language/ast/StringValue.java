package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class StringValue extends Value {
    public StringValue(Location loc, String value) {
        super(loc);
    }
}
