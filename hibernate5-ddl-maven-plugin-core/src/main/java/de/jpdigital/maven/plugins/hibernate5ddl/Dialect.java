/*
 * Copyright (C) 2014 Jens Pelzetter <jens.pelzetter@googlemail.com>
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
package de.jpdigital.maven.plugins.hibernate5ddl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This enumeration provides constants for all dialects supported by Hibernate.
 *
 * The dialects supported by Hibernate can be found in the <a href=
 * "http://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#database-dialect">
 * Hibernate documentation</a>. Also this enumeration provides the convenient
 * method {@link #getDialectClass()} for getting the class name of the Hibernate
 * dialect.
 * 
 * Please note that not all supported Hibernate versions support every of these
 * dialects.
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
public class Dialect {
	private static Map<String, String> map = new HashMap<>();
	static {
		map.put("ABSTRACT_HANA", "org.hibernate.dialect.AbstractHANADialect");
		map.put("CACHE71", "org.hibernate.dialect.Cache71Dialect");
		map.put("CUBRID", "org.hibernate.dialect.CUBRIDDialect");
		map.put("DB2", "org.hibernate.dialect.DB2Dialect");
		map.put("DB297", "org.hibernate.dialect.DB207");
		map.put("DB2900", "org.hibernate.dialect.DB2390Dialect");
		map.put("DB2400", "org.hibernate.dialect.DB2400Dialect");
		map.put("DB2_AS400", "org.hibernate.dialect.DB2400Dialect");
		map.put("DB2_OS390", "org.hibernate.dialect.DB2390Dialect");
		map.put("DERBY_10_5", "org.hibernate.dialect.DerbyTenFiveDialect");
		map.put("DERBY_10_6", "org.hibernate.dialect.DerbyTenSixDialect");
		map.put("DERBY_10_7", "org.hibernate.dialect.DerbyTenSevenDialect");
		map.put("FIREBIRD", "org.hibernate.dialect.FirebirdDialect");
		map.put("FRONTBASE", "org.hibernate.dialect.FrontBaseDialect");
		map.put("H2", "org.hibernate.dialect.H2Dialect");
		map.put("HANA_COLUMN_STORE", "org.hibernate.dialect.HanaColumnStoreDialect");
		map.put("HANA_ROW_STORE", "org.hibernate.dialect.HanaRowStoreDialect");
		map.put("HSQL", "org.hibernate.dialect.HSQLDialect");
		map.put("INFORMIX", "org.hibernate.dialect.InformixDialect");
		map.put("INGRES", "org.hibernate.dialect.IngresDialect");
		map.put("INGRES9", "org.hibernate.dialect.Ingres9Dialect");
		map.put("INGRES10", "org.hibernate.dialect.Ingres10Dialect");
		map.put("INTERBASE", "org.hibernate.dialect.InterbaseDialect");
		map.put("INTERSYSTEMS_CACHE", "org.hibernate.dialect.Cache71Dialect");
		map.put("JDATASTORE", "org.hibernate.dialect.JDataStoreDialect");
		map.put("MCKOISQL", "org.hibernate.dialect.MckoiDialect");
		map.put("MIMERSQL", "org.hibernate.dialect.MimerSQLDialect");
		map.put("MYSQL", "org.hibernate.dialect.MySQLDialect");
		map.put("MYSQL_INNODB", "org.hibernate.dialect.MySQLInnoDBDialect");
		map.put("MYSQL_MYISAM", "org.hibernate.dialect.MySQLMyISAMDialect");
		map.put("MYSQL5", "org.hibernate.dialect.MySQL5Dialect");
		map.put("MYSQL5_INNODB", "org.hibernate.dialect.MySQL5InnoDBDialect");
		map.put("MYSQL57_INNODB", "org.hibernate.dialect.MySQL57InnoDBDialect");
		map.put("ORACLE8I", "org.hibernate.dialect.Oracle8iDialect");
		map.put("ORACLE9I", "org.hibernate.dialect.Oracle9iDialect");
		map.put("ORACLE10G", "org.hibernate.dialect.Oracle10gDialect");
		map.put("ORACLE12C", "org.hibernate.dialect.Oracle12cDialect");
		map.put("ORACLE_TIMES_TEN", "org.hibernate.dialect.TimesTenDialect");
		map.put("POINTBASE", "org.hibernate.dialect.PointbaseDialect");
		map.put("POSTGRES_PLUS", "org.hibernate.dialect.PostgresPlusDialect");
		map.put("POSTGRESQL81", "org.hibernate.dialect.PostgreSQL81Dialect");
		map.put("POSTGRESQL82", "org.hibernate.dialect.PostgreSQL82Dialect");
		map.put("POSTGRESQL9", "org.hibernate.dialect.PostgreSQL9Dialect");
		map.put("PROGRESS", "org.hibernate.dialect.ProgressDialect");
		map.put("SAP_DB", "org.hibernate.dialect.SAPDBDialect");
		map.put("SAP_HANA_COL", "org.hibernate.dialect.HANAColumnStoreDialect");
		map.put("SAP_HANA_ROW", "org.hibernate.dialect.HANARowStoreDialect");
		map.put("SQLSERVER2000", "org.hibernate.dialect.SQLServerDialect");
		map.put("SQLSERVER2005", "org.hibernate.dialect.SQLServer2005Dialect");
		map.put("SQLSERVER2008", "org.hibernate.dialect.SQLServer2008Dialect");
		map.put("SQLSERVER2012", "org.hibernate.dialect.SQLServer2012Dialect");
		map.put("SYBASE", "org.hibernate.dialect.SybaseDialect");
		map.put("SYBASE11", "org.hibernate.dialect.Sybase11Dialect");
		map.put("SYBASE_ASE155", "org.hibernate.dialect.SybaseASE15Dialect");
		map.put("SYBASE_ASE157", "org.hibernate.dialect.SybaseASE157Dialect");
		map.put("SYBASE_ANYWHERE", "org.hibernate.dialect.SybaseAnywhereDialect");
		map.put("TERADATA", "org.hibernate.dialect.TeradataDialect");
		map.put("UNISYS_OS_2200_RDMS", "org.hibernate.dialect.RDMSOS2200Dialect");
	}


	/**
	 * Property for holding the name of the Hibernate dialect class.
	 */
	private final String dialectCode;
	/**
	 * Property for holding the name of the Hibernate dialect class.
	 */
	private final String dialectClass;

	/**
	 * Private constructor, used to create the Enum instances for each dialect.
	 *
	 * @param dialectClass
	 *            The dialect class for the specific dialect.
	 */
	public Dialect(final String dialectClass) {
		String dialect = dialectClass.toUpperCase(Locale.ENGLISH);
		if (map.containsKey(dialect)) {
			this.dialectClass = map.get(dialect);
			this.dialectCode = dialect.toLowerCase(Locale.ENGLISH);
		} else {
			try {
				this.getClass().getClassLoader().loadClass(dialectClass);
			} catch (Exception e) {
				throw new IllegalArgumentException(dialectClass);
			}
			this.dialectClass = dialectClass;
			this.dialectCode = null;
		}
	}

	/**
	 * Getter for the dialect class.
	 *
	 * @return The name of the dialect class, for example
	 *         {@code org.hibernate.dialect.PostgreSQL9Dialect}
	 */
	public String getDialectClass() {
		return dialectClass;
	}

	public String name() {
		if (dialectCode != null) {
			return dialectCode;
		} else {
			String cls[] = dialectClass.split("\\.");
			String simpleName = cls[cls.length - 1];
			return simpleName.toLowerCase(Locale.ENGLISH);
		}
	}

	public static Set<String> values() {
		return map.keySet();
	}
}
