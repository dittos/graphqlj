package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

import java.util.List;

public class OperationDefinition extends Node {
    public OperationDefinition(Location loc, String operation, Name name, List<VariableDefinition> variableDefinitions, List<Directive> directives, SelectionSet selectionSet) {
        super(loc);
    }
}
