component {
    this.name = "LDEV-4380"
    this.ormEnabled = true;
    this.ormSettings = {
        datasource: "testh2",
        dbcreate: "update",
        dialect: "h2",
        eventHandling: true,
        eventHandler: "eventHandler"
    };

    this.datasources["testH2"] = server.getDatasource("h2", server._getTempDir("LDEV4380"));
    this.datasource = "testH2";

    public function onRequestStart() {
        query {
            echo("INSERT INTO test4380(A) VALUES( 'testA' )");
        }
    }
}