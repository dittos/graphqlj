package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

import java.util.List;

public class FragmentSpread extends Fragment {
    public FragmentSpread(Location loc, Name name, List<Directive> directives) {
        super(loc);
    }
}
