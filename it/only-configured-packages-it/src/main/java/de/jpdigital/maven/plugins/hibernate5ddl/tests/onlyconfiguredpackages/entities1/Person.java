/*
 * Copyright (C) 2014 Jens Pelzetter
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
package de.jpdigital.maven.plugins.hibernate5ddl.tests.onlyconfiguredpackages.entities1;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Jens Pelzetter <jens.pelzetter@googlemail.com>
 */
@Entity
@Table(name = "persons")
public class Person implements Serializable {

    private static final long serialVersionUID = -6805988424810784605L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long personId;

    @Column(name = "name_prefix")
    private String prefix;
    @Column(name = "suffix")
    private String suffix;
    @Column(name = "surname")
    private String surname;
    @Column(name = "given_name")
    private String givenName;
    @Embedded
    private TextMixin description;

    public Person() {
        //Nothing
    }

    public Person(final String surname, final String givenName) {
        this.surname = surname;
        this.givenName = givenName;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(final long personId) {
        this.personId = personId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(final String givenName) {
        this.givenName = givenName;
    }
    
    public TextMixin getDescription() {
        return description;
    }
    
    public void setDescription(final TextMixin description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(prefix);
        hash = 29 * hash + Objects.hashCode(suffix);
        hash = 29 * hash + Objects.hashCode(surname);
        hash = 29 * hash + Objects.hashCode(givenName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Person other = (Person) obj;
        if (!Objects.equals(this.prefix, other.getPrefix())) {
            return false;
        }
        if (!Objects.equals(this.suffix, other.getSuffix())) {
            return false;
        }
        if (!Objects.equals(this.surname, other.getSurname())) {
            return false;
        }
        if (!Objects.equals(this.givenName, other.getGivenName())) {
            return false;
        }
        return true;
    }

}

