component {
    this.name = "LDEV-3860";

    mssql = getDatasource();
    this.datasource = mssql;

    this.ormenabled = true;
    this.ormsettings = {
        dbcreate="dropCreate"
        ,dialect="MicrosoftSQLServer"
    }

    public function onRequestStart() {
        if (StructIsEmpty(mssql)) {
            writeoutput("Datasource credentials not available");
            return false;
        }
    }
    public function onRequestEnd() {
        if (StructIsEmpty(mssql)) return;
    }

    private struct function getDatasource(){
        return server.getDatasource("mssql");
    }
}