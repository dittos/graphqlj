package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

import java.util.List;

public class InlineFragment extends Fragment {
    public InlineFragment(Location loc, Name typeCondition, List<Directive> directives, SelectionSet selectionSet) {
        super(loc);
    }
}
