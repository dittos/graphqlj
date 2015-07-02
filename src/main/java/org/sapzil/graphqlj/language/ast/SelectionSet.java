package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

import java.util.List;

public class SelectionSet extends Node {
    public SelectionSet(Location loc, List<Selection> selections) {
        super(loc);
    }
}
