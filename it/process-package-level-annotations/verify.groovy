import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.StandardCharsets

def dialects = ["hsql", "mysql5", "postgresql9"]

for (def dialect : dialects) {
    def file = new File(
        basedir, 
        String.format(
            "target/generated-resources/sql/ddl/auto/%s.sql", 
            dialect.toLowerCase()
        )
    );

    if (!file.exists()) {
        throw new FileNotFoundException(
            String.format(
                "DDL file '%s' for dialect '%s' does not exist.",
                file.getAbsolutePath(),
                dialect.toLowerCase()
            )
        )
    }

    def ddlScriptBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
    def ddlScript = new String(ddlScriptBytes, StandardCharsets.UTF_8);

    def tables = ["some_object"];

    for (def table : tables) {
        if (!ddlScript.toLowerCase().contains(
            String.format("create table %s", table)
        )) {
            throw new RuntimeException(
                String.format(
                    "DDL script does contain a create statement for table '%s'.",
                    table
                )
            )
        }

        if (ddlScript.toLowerCase().contains(
            String.format("drop table if exists %s", table)
        )) {
            throw new RuntimeException(
                String.format(
                    "DDL script contains a drop statement for table '%s', " 
                        + "but drop statements are not enabled.",
                    table
                )
            )
        }

        if (ddlScript.toLowerCase().contains(
            String.format("create table %s_revisions", table)
        )) {
            throw new RuntimeException(
                String.format(
                    "DDL script contains a envers revisions table for table "
                        + "%s but Envers is not enabled.",
                    table
                )
            )
        }
    }
}
