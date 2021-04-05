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
import javax.persistence.Embeddable;

/**
 *
 * @author Jens Pelzetter <jens.pelzetter@googlemail.com>
 */
@Embeddable
public class TextMixin implements Serializable {

    private static final long serialVersionUID = -3104708823219990153L;

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.text);
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
        final TextMixin other = (TextMixin) obj;
        if (!Objects.equals(this.text, other.getText())) {
            return false;
        }
        return true;
    }

}
