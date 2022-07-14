component {
    this.name = createUUID();
    this.datasource = "testMysql";
    
    this.ormEnabled = true;
    this.ormSettings = {
        dbcreate = "dropCreate",
        dialect = "MySQL"
    };

    this.datasources["testMysql"] = server.getdatasource("mysql");
}