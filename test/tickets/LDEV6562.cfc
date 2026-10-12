component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource,pool" {

	function beforeAll() {
		variables.adminPassword = request.ServerAdminPassword;
		variables.prefix  = "LDEV6562_";
		variables.rounds  = 5;
		variables.threads = 20;
		if ( isH2NotSupported() ) return;
		for ( var r = 1; r <= variables.rounds; r++ ) {
			registerServerDatasource( variables.prefix & r, "jdbc:h2:mem:#variables.prefix##r#" );
		}
	}

	function afterAll() {
		if ( isH2NotSupported() ) return;
		for ( var r = 1; r <= variables.rounds; r++ ) {
			removeServerDatasourceIfExists( variables.prefix & r );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6562 concurrent first use of a datasource creates a single pool", function() {

			it( title="concurrent queries against a fresh datasource create exactly one pool", skip=isH2NotSupported(), body=function() {
				// several fresh datasources, so a lucky schedule in one round can't hide the race
				for ( var r = 1; r <= variables.rounds; r++ ) {
					var dsName = variables.prefix & r;
					expect( poolCountForName( dsName ) ).toBe( 0, "no pool before first use of #dsName#" );

					var barrier = createObject( "java", "java.util.concurrent.CountDownLatch" ).init( javaCast( "int", 1 ) );
					var names = [];
					for ( var i = 1; i <= variables.threads; i++ ) {
						var tn = "ldev6562_#r#_#i#_#createUUID()#";
						arrayAppend( names, tn );
						thread name=tn b=barrier ds=dsName {
							attributes.b.await();
							queryExecute( "select 1", {}, { datasource: attributes.ds } );
						}
					}
					sleep( 200 ); // let all threads park on the latch
					barrier.countDown();
					thread action="join" name=arrayToList( names ) timeout=30000;

					for ( var tn in names ) {
						var t = cfthread[ tn ];
						expect( t.status ).toBe( "COMPLETED", "thread #tn# status #t.status# #t.error.message ?: ''#" );
					}
					expect( poolCountForName( dsName ) ).toBe( 1, "exactly one pool for #dsName#" );
				}
			});
		});
	}

	private numeric function poolCountForName( required string dsName ) {
		var iter = getPageContext().getConfig().getDatasourceConnectionPools().iterator();
		var n = 0;
		while ( iter.hasNext() ) {
			var p = iter.next();
			if ( p.getFactory().getDatasource().getName() == arguments.dsName ) n++;
		}
		return n;
	}

	private void function registerServerDatasource( required string name, required string connectionString ) {
		admin
			action="updateDatasource"
			type="server"
			password="#variables.adminPassword#"
			name="#arguments.name#"
			newName="#arguments.name#"
			classname="org.h2.Driver"
			bundlename="org.lucee.h2"
			dsn="#arguments.connectionString#"
			dbusername="sa"
			dbpassword=""
			connectionLimit="30"
			connectionTimeout="0"
			blob="false"
			clob="false"
			validate="false"
			storage="false"
			allowed_select="true"
			allowed_insert="true"
			allowed_update="true"
			allowed_delete="true"
			allowed_alter="true"
			allowed_drop="true"
			allowed_revoke="true"
			allowed_create="true"
			allowed_grant="true";
	}

	private void function removeServerDatasourceIfExists( required string name ) {
		try {
			admin action="removeDatasource" type="server" password="#variables.adminPassword#" name="#arguments.name#";
		}
		catch ( any e ) {}
	}

	private boolean function isH2NotSupported() {
		try {
			var ds = server.getTestService( "h2", server._getTempDir( "LDEV6562" ) );
			return structIsEmpty( ds );
		}
		catch ( any e ) {
			return true;
		}
	}

}
