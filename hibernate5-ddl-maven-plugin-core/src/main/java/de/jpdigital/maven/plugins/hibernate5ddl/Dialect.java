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

/**
 * This enumeration provides constants for all dialects supported by Hibernate.
 *
 * The dialects supported by Hibernate can be found in the
 * <a href="http://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#database-dialect">
 * Hibernate documentation</a>. Also this enumeration provides the convenient
 * method {@link #getDialectClass()} for getting the class name of the Hibernate
 * dialect.
 * 
 * Please note that not all supported Hibernate versions support every of these
 * dialects.
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
public enum Dialect {

    ABSTRACT_HANA("org.hibernate.dialect.AbstractHANADialect"),
    CACHE71("org.hibernate.dialect.Cache71Dialect"),
    CUBRID("org.hibernate.dialect.CUBRIDDialect"),
    DATA_DIRECT_ORACLE9("org.hibernate.dialect.DataDirectOracle9Dialect"),
    DB2("org.hibernate.dialect.DB2Dialect"),
    DB297("org.hibernate.dialect.DB207"),
    DB2390("org.hibernate.dialect.DB2390Dialect"),
    DB2400("org.hibernate.dialect.DB2400Dialect"),
    DB2_AS400("org.hibernate.dialect.DB2400Dialect"),
    DB2_OS390("org.hibernate.dialect.DB2390Dialect"),
    DERBY("org.hibernate.dialect.DertyDialect.class.getName()"),
    DERBY_10_5("org.hibernate.dialect.DerbyTenFiveDialect"),
    DERBY_10_6("org.hibernate.dialect.DerbyTenSixDialect"),
    DERBY_10_7("org.hibernate.dialect.DerbyTenSevenDialect"),
    FIREBIRD("org.hibernate.dialect.FirebirdDialect"),
    FRONTBASE("org.hibernate.dialect.FrontBaseDialect"),
    H2("org.hibernate.dialect.H2Dialect"),
    HANA_COLUMN_STORE("org.hibernate.dialect.HanaColumnStoreDialect"),
    HANA_ROW_STORE("org.hibernate.dialect.HanaRowStoreDialect"),
    HSQL("org.hibernate.dialect.HSQLDialect"),
    INFORMIX("org.hibernate.dialect.InformixDialect"),
    INFORMIX10("org.hibernate.dialect.Informix10Dialect"),
    INGRES("org.hibernate.dialect.IngresDialect"),
    INGRES9("org.hibernate.dialect.Ingres9Dialect"),
    INGRES10("org.hibernate.dialect.Ingres10Dialect"),
    INTERBASE("org.hibernate.dialect.InterbaseDialect"),
    INTERSYSTEMS_CACHE("org.hibernate.dialect.Cache71Dialect"),
    JDATASTORE("org.hibernate.dialect.JDataStoreDialect"),
    MARIADB("org.hibernate.dialect.MariaDBDialect"),
    MARIADB53("org.hibernate.dialect.MariaDB53Dialect"),
    MCKOISQL("org.hibernate.dialect.MckoiDialect"),
    MIMERSQL("org.hibernate.dialect.MimerSQLDialect"),
    MYSQL("org.hibernate.dialect.MySQLDialect"),
    MYSQL_INNODB("org.hibernate.dialect.MySQLInnoDBDialect"),
    MYSQL_MYISAM("org.hibernate.dialect.MySQLMyISAMDialect"),
    MYSQL5("org.hibernate.dialect.MySQL5Dialect"),
    MYSQL5_SPATIAL("org.hibernate.spatial.dialect.mysql.MySQL5SpatialDialect"),
    MYSQL5_INNODB_SPATIAL("org.hibernate.spatial.dialect.mysql.MySQL5InnoDBSpatialDialect"),
    MYSQL5_INNODB("org.hibernate.dialect.MySQL5InnoDBDialect"),
    MYSQL55("org.hibernate.dialect.MySQL55Dialect"),
    MYSQL57("org.hibernate.dialect.MySQL57Dialect"),
    MYSQL57_INNODB("org.hibernate.dialect.MySQL57InnoDBDialect"),
    ORACLE("org.hibernate.dialect.OracleDialect"),
    ORACLE8I("org.hibernate.dialect.Oracle8iDialect"),
    ORACLE9("org.hibernate.dialect.Oracle9Dialect"),
    ORACLE9I("org.hibernate.dialect.Oracle9iDialect"),
    ORACLE10G("org.hibernate.dialect.Oracle10gDialect"),
    ORACLE12C("org.hibernate.dialect.Oracle12cDialect"),
    ORACLE_SPATIAL_10G("org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect"),
    ORACLE_SPATIAL_SDO_10G("org.hibernate.spatial.dialect.oracle.OracleSpatialSDO10gDialect"),
    ORACLE_TIMES_TEN("org.hibernate.dialect.TimesTenDialect"),
    POINTBASE("org.hibernate.dialect.PointbaseDialect"),
    POSTGIS_PG82("org.hibernate.spatial.dialect.postgis.PostgisPG82Dialect"),
    POSTGIS_PG9("org.hibernate.spatial.dialect.postgis.PostgisPG9Dialect"),
    POSTGIS_PG91("org.hibernate.spatial.dialect.postgis.PostgisPG91Dialect"),
    POSTGRES_PLUS("org.hibernate.dialect.PostgresPlusDialect"),
    POSTGRESQL("org.hibernate.dialect.PostgreSQLDialect"),
    POSTGRESQL81("org.hibernate.dialect.PostgreSQL81Dialect"),
    POSTGRESQL82("org.hibernate.dialect.PostgreSQL82Dialect"),
    POSTGRESQL9("org.hibernate.dialect.PostgreSQL9Dialect"),
    POSTGRESQL91("org.hibernate.dialect.PostgreSQL91Dialect"),
    POSTGRESQL92("org.hibernate.dialect.PostgreSQL92Dialect"),
    PROGRESS("org.hibernate.dialect.ProgressDialect"),
    RDMSOS2200("org.hibernate.dialect.RDMSOS2200Dialect"),
    SAP_DB("org.hibernate.dialect.SAPDBDialect"),
    SAP_HANA_COL("org.hibernate.dialect.HANAColumnStoreDialect"),
    SAP_HANA_ROW("org.hibernate.dialect.HANARowStoreDialect"),
    SQLSERVER2000("org.hibernate.dialect.SQLServerDialect"),
    SQLSERVER2005("org.hibernate.dialect.SQLServer2005Dialect"),
    SQLSERVER2008("org.hibernate.dialect.SQLServer2008Dialect"),
    SQLSERVER2008_SPATIAL("org.hibernate.dialect.SQLServer2008SpatialDialect"),
    SQLSERVER2012("org.hibernate.dialect.SQLServer2012Dialect"),
    SYBASE("org.hibernate.dialect.SybaseDialect"),
    SYBASE11("org.hibernate.dialect.Sybase11Dialect"),
    SYBASE_ASE155("org.hibernate.dialect.SybaseASE15Dialect"),
    SYBASE_ASE157("org.hibernate.dialect.SybaseASE157Dialect"),
    SYBASE_ANYWHERE("org.hibernate.dialect.SybaseAnywhereDialect"),
    TERADATA("org.hibernate.dialect.TeradataDialect"),
    TERADATA14("org.hibernate.dialect.Teradata14Dialect"),
    UNISYS_OS_2200_RDMS("org.hibernate.dialect.RDMSOS2200Dialect");

    /**
     * Property for holding the name of the Hibernate dialect class.
     */
    private final String dialectClass;

    /**
     * Private constructor, used to create the Enum instances for each dialect.
     *
     * @param dialectClass The dialect class for the specific dialect.
     */
    private Dialect(final String dialectClass) {
        this.dialectClass = dialectClass;
    }

    /**
     * Getter for the dialect class.
     *
     * @return The name of the dialect class, for example
     *         {@code org.hibernate.dialect.PostgreSQL9Dialect} for
     *         {@link #POSTGRESQL9}.
     */
    public String getDialectClass() {
        return dialectClass;
    }

}
