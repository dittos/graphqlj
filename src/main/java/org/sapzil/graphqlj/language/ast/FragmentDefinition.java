package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

import java.util.List;

public class FragmentDefinition extends Node {
    public FragmentDefinition(Location loc, Name name, Name typeCondition, List<Directive> directives, SelectionSet selectionSet) {
        super(loc);
    }
}
