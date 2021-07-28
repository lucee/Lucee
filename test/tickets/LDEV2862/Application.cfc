component {
	this.name = "LDEV-2862";
    this.ormEnabled = "true";
    this.ormSettings = {
        dbCreate = "update"
    }
    this.datasource = server.getDatasource("h2");

    public function onRequestStart() {
        query result="test"{
            echo("INSERT INTO test(A) VALUES( 'testA' )");
        }
        query result="test2"{
            echo("INSERT INTO test2(B) VALUES( 'testB' )");
        }
        query {
            echo("INSERT INTO okok(testid, id) VALUES( #test.GENERATEDKEY#, #test2.GENERATEDKEY# )");
        }
    }
    public function onRequestEnd() {
        query {
            echo("DROP TABLE test");
        }
        query {
            echo("DROP TABLE test2");
        }
        query {
            echo("DROP TABLE okok");
        }
    }
}