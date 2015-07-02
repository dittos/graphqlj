package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

import java.util.List;

public class Field extends Selection {
    public Field(Location loc, Name alias, Name name, List<Argument> arguments, List<Directive> directives, SelectionSet selectionSet) {
        super(loc);
    }
}
