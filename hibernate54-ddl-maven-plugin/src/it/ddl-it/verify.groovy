def hsqlFile = new File(basedir, "target/generated-resources/sql/ddl/auto/hsql.sql")
def mysql5File = new File(basedir, "target/generated-resources/sql/ddl/auto/mysql5.sql")
def postgresql9File = new File(basedir, "target/generated-resources/sql/ddl/auto/postgresql9.sql")

if (!hsqlFile.exists()) {
    throw new FileNotFoundException(
        String.format(
            "DDL file '%s' for HSQL dialect not generated.",
            hsqlFile.getAbsolutePath()
        )
    );
}
if (!mysql5File.exists()) {
    throw new FileNotFoundException(
        String.format(
            "DDL file %s for MySQL dialect not generated.",
            mysqlFile.getAbsolutePath()
        )
    );
}
if (!postgresql9File.exists()) {
    throw new FileNotFoundException(
        String.format(
            "DDL file %s for POSTGRESQL9 dialect not generated.",
            postgresql9File.getAbsolutePath()
        )
    );
}
