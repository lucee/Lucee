component {
    mySQL = getDatasource();
    this.name = "LDEV-3487";
    this.datasources["LDEV_3487"] = mySQL;
    this.datasource = "LDEV_3487";
    public function onRequestStart(){
        if( StructIsEmpty(mySQL) ){
            writeoutput("Datasource credentials was not available"); // Datasource credentials was not available means need to skip the iteration.
        abort;
        }
        query{
            echo("DROP TABLE IF EXISTS LDEV3487_MYSQL");
        }
        query{
            echo("CREATE TABLE LDEV3487_MYSQL( id int NOT NULL AUTO_INCREMENT PRIMARY KEY, test VARCHAR(50))");
        }
    }
    public function onRequestEnd(){
        if( StructIsEmpty(mySQL) ) return;
        query{
            echo("DROP TABLE IF EXISTS LDEV3487_MYSQL");
        }
    }
    private struct function getDatasource(){
        return server.getDatasource("mysql");
    }
}