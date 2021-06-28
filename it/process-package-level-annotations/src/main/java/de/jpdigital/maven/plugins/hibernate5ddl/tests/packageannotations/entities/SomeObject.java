/*
 * Copyright (C) 2021 Jens Pelzetter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jpdigital.maven.plugins.hibernate5ddl.tests.packageannotations.entities;

import de.jpdigital.maven.plugins.hibernate5ddl.tests.packageannotations.types.BitSetType;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.BitSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.BitSet;

@Entity
@Table(name = "some_objects")
public class SomeObject implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long objectId;

    @Type(type
        = "de.jpdigital.maven.plugins.hibernate5ddl.tests.packageannotations.types.BitSetType")
    private BitSet bitSet;

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public void setBitSet(BitSet bitSet) {
        this.bitSet = bitSet;
    }

}
