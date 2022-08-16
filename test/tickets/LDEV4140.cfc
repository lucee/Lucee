component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql" skip=true {
    function run( testResults , testBox ) {
        describe( "Test suite for LDEV-4140", function() {
            it( title="Checking multiple queries with the nested errors in MSSQL", skip="#notHasMssql()#", body = function( currentSpec ) {
                try {
                    query datasource="#server.getDatasource("mssql")#" {
                        echo(
                            "RAISERROR (15600, -1, -1, 'Boom1');
                            RAISERROR (15600, -1, -1, 'Boom2');
                            RAISERROR (15600, -1, -1, 'Boom3');"
                        )
                    }
                }
                catch(any e) {
                    var err = e;
                }
                expect(err).toHaveKey("cause");
                expect(isStruct(err.cause)).toBeTrue();
                expect(err.cause.ErrorCode).toBe(15600);
                expect(err.cause.Message).toInclude("Boom1");

                expect(err.cause).toHaveKey("NextException");
                expect(isStruct(err.cause.NextException)).toBeTrue();
                expect(err.cause.NextException.ErrorCode).toBe(15600);
                expect(err.cause.NextException.Message).toInclude("Boom2");

                expect(err.cause.NextException).toHaveKey("NextException");
                expect(isStruct(err.cause.NextException.NextException)).toBeTrue();
                expect(err.cause.NextException.NextException.ErrorCode).toBe(15600);
                expect(err.cause.NextException.NextException.Message).toInclude("Boom3");

            });
        });
    }

    private function notHasMssql() {
        return structCount(server.getDatasource("mssql")) == 0;
    }
} 