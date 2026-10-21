component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource,pool" {

	function beforeAll() {
		variables.adminPassword = request.ServerAdminPassword;
		variables.dsName = "LDEV6567_" & left( hash( createUUID() ), 8 );
		if ( isH2NotSupported() ) return;
		admin
			action="updateDatasource"
			type="server"
			password="#variables.adminPassword#"
			name="#variables.dsName#"
			newName="#variables.dsName#"
			classname="org.h2.Driver"
			bundlename="org.lucee.h2"
			dsn="jdbc:h2:mem:#variables.dsName#"
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

	function afterAll() {
		if ( isH2NotSupported() ) return;
		try {
			admin action="removeDatasource" type="server" password="#variables.adminPassword#" name="#variables.dsName#";
		}
		catch ( any e ) {}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6567 datasource pool creation never replaces an existing pool", function() {

			it( title="concurrent pool lookups all get the same pool instance", skip=isH2NotSupported(), body=function() {
				var config = getPageContext().getConfig();
				var ds = config.getDataSource( variables.dsName );
				var ids = createObject( "java", "java.util.concurrent.ConcurrentHashMap" ).init();
				var barrier = createObject( "java", "java.util.concurrent.CountDownLatch" ).init( javaCast( "int", 1 ) );
				var names = [];
				for ( var i = 1; i <= 20; i++ ) {
					var tn = "ldev6567_#i#_#createUUID()#";
					arrayAppend( names, tn );
					thread name=tn b=barrier ds=ds ids=ids {
						attributes.b.await();
						var p = getPageContext().getConfig().getDatasourceConnectionPool( attributes.ds, "sa", "" );
						attributes.ids.put( createObject( "java", "java.lang.System" ).identityHashCode( p ), true );
					}
				}
				sleep( 200 );
				barrier.countDown();
				thread action="join" name=arrayToList( names ) timeout=30000;
				for ( var tn in names ) {
					expect( cfthread[ tn ].status ).toBe( "COMPLETED", "thread #tn#: #cfthread[ tn ].error.message ?: ''#" );
				}
				expect( ids.size() ).toBe( 1 );
				var p1 = config.getDatasourceConnectionPool( ds, "sa", "" );
				var p2 = config.getDatasourceConnectionPool( ds, "sa", "" );
				expect( p1.equals( p2 ) ).toBeTrue();
				expect( poolCountForName( variables.dsName ) ).toBe( 1 );
				queryExecute( "select 1", {}, { datasource: variables.dsName } );
			});
		});
	}

	private numeric function poolCountForName( required string dsName ) {
		var iter = getPageContext().getConfig().getDatasourceConnectionPools().iterator();
		var n = 0;
		while ( iter.hasNext() ) {
			if ( iter.next().getFactory().getDatasource().getName() == arguments.dsName ) n++;
		}
		return n;
	}

	private boolean function isH2NotSupported() {
		try {
			return structIsEmpty( server.getTestService( "h2", server._getTempDir( "LDEV6567" ) ) );
		}
		catch ( any e ) {
			return true;
		}
	}

}
