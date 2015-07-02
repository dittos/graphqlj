package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class EnumValue extends Value {
    public EnumValue(Location loc, String value) {
        super(loc);
    }
}
