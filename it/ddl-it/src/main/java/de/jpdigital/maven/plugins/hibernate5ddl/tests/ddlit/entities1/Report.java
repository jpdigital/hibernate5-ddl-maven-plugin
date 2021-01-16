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
package de.jpdigital.maven.plugins.hibernate5ddl.tests.ddlit.entities1;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
// import org.hibernate.envers.AuditTable;
// import org.hibernate.envers.Audited;

/**
 *
 * @author Jens Pelzetter <jens.pelzetter@googlemail.com>
 */
@Entity
@Table(name = "reports")
// @Audited
// @AuditTable("reports_revisions")
public class Report implements Serializable {

    private static final long serialVersionUID = 1017452107957555070L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long reportId;
    
    @Column(name = "title")
    private String title;
    @Column(name = "content")
    private String content;

    public long getReportId() {
        return reportId;
    }

    public void setReportId(final long reportId) {
        this.reportId = reportId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }
    
    public void setContent(final String content) {
        this.content = content;
    }

}
