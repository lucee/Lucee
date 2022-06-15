component {
    this.name = "LDEV-3406";

    this.datasource = server.getDatasource("h2", "#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db");

    public function onRequestStart() {
        query {
            echo("DROP TABLE IF EXISTS LDEV3406");
        }
        query {
            echo(
                "CREATE TABLE LDEV3406(
                intCol int,
                bitCol bit,
                varcharCol varchar(50))"
            );
        }
        query {
            echo("INSERT INTO LDEV3406(intCol, bitCol, varcharCol) VALUES(1000, 0, 1000),('1000', '0', '1000')");
        }
    }
}