component extends="org.lucee.cfml.test.LuceeTestCase" {

    function beforeAll() {
        variables.mssql = getCredentials();
    }

    function run( testResults , testBox ) {
        describe( "Test suite for LDEV-4827", function() {
            it( title='Checking Query with real result on mssql', skip=checkMySqlEnvVarsAvailable(), body=function( currentSpec ) {
                adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
                adm.updateDatasource(
                    name: 'datasource4827',
                    newname: 'datasource4827',
                    type: 'MSSQL',
                    host: '#mssql.SERVER#',
                    database: '#mssql.DATABASE#',
                    port: '#mssql.PORT#',
                    username: '#mssql.USERNAME#',
                    password: '#mssql.PASSWORD#',
                    connectionTimeout: 12,
                    storage: false,
                    blob: true,
                    clob: true
                );

                ```
                    <!--- mssql --->
                    <cfquery name="test" datasource="datasource4827">
                        SELECT CAST(17.8 AS REAL) AS testReal
                    </cfquery>
                ```

                expect(test.testReal).toBe(17.8);
            });

        });
    }

    private boolean function checkMySqlEnvVarsAvailable() {
        return (StructCount(server.getDatasource("mssql")) eq 0);
    }

    private struct function getCredentials() {
        // getting the credentials from the environment variables
        var mssql = {};
        mssql = server.getDatasource(service="mssql", onlyConfig=true);
        return mssql;
    }

    function afterAll() {
        if(!isNull(adm)) {
            adm.removeDatasource(
                dsn: 'datasource4827',
                remoteClients: "arrayOfClients"
            );
        }
    }

}
