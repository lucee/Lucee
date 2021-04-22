component {
    pgSQL = getCredentials();
    this.name = "test3426";
    this.datasources["LDEV_3426"] = pgSQL;
    this.datasource = "LDEV_3426"
    public function onRequestStart(){
        if( StructIsEmpty(pgSQL) ){
            writeoutput("Datasource credentials was not available"); // Datasource credentials was not available means need to stop the iterations.
            abort;
        }
        query{
        echo("DROP TABLE IF EXISTS LDEV3426_test");
        }
        query{
            echo("DROP TABLE IF EXISTS LDEV3426_Primary");
        }
        query{
            echo("CREATE TABLE LDEV3426_Primary( id SERIAL NOT NULL PRIMARY KEY, test VARCHAR(50))");
        }
    }
    private struct function getCredentials() {
        return server.getDatasource("postgres");
    }
    public function onRequestEnd(){
        if( StructIsEmpty(pgSQL) ) return;
        query{
            echo("DROP TABLE IF EXISTS LDEV3426_test");
        }
        query{
            echo("DROP TABLE IF EXISTS LDEV3426_Primary");
        }
    }
}