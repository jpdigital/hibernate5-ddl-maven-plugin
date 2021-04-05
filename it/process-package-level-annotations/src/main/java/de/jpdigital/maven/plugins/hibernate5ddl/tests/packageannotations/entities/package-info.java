@TypeDefs(
    {
        @TypeDef(
            name = "bitset",
            defaultForType = BitSet.class,
            typeClass = BitSetType.class
        )
    }
)
package de.jpdigital.maven.plugins.hibernate5ddl.tests.packageannotations.entities;

import de.jpdigital.maven.plugins.hibernate5ddl.tests.packageannotations.types.BitSetType;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import java.util.BitSet;
