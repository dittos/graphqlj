package org.sapzil.graphqlj.language.ast;

import org.sapzil.graphqlj.language.Location;

public abstract class Fragment extends Selection {
    public Fragment(Location loc) {
        super(loc);
    }
}
