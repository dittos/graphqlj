package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public class VariableDefinition extends Node {
    public VariableDefinition(Location loc, Variable variable, Type type, Value defaultValue) {
        super(loc);
    }
}
