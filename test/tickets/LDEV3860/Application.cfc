component {
    this.name = "LDEV-3860";
    this.ormenabled = true;
    this.ormsettings = {
        dbcreate="dropCreate"
        ,dialect="MicrosoftSQLServer"
    } 
    mssql = getDatasource();
    this.datasources["LDEV_3860"] = mssql;
    this.datasource = "LDEV_3860";

    public function onRequestStart() {
        if (StructIsEmpty(mssql)) {
            writeoutput("Datasource credentials was not available"); // Datasource credentials was not available means need to skip the iteration.
            abort;
        }
    }
    public function onRequestEnd() {
        if (StructIsEmpty(mssql)) return;
    }

    private struct function getDatasource(){
        return server.getDatasource("mssql");
    } 
}