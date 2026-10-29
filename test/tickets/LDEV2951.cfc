component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-2951 getCacheConnections includes application caches", function() {

			it( title = "lists the application defined cache and keeps the administrator caches", body = function( currentSpec ) {
				var adminName = "ldev2951admin";
				createAdminCache( adminName, 111 );
				try {
					var data = request( "list.cfm" );
					expect( data.names ).toInclude( "ldev2951app" );
					expect( data.names ).toInclude( "ldev2951shared" );
					expect( data.names ).toInclude( adminName );
				}
				finally {
					removeAdminCache( adminName );
				}
			} );

			it( title = "an application cache overrides an administrator cache with the same name", body = function( currentSpec ) {
				createAdminCache( "ldev2951shared", 111 );
				try {
					var data = request( "list.cfm" );
					// the application defines 222, the administrator 111
					expect( data.sharedTTL ).toBe( 222 );
				}
				finally {
					removeAdminCache( "ldev2951shared" );
				}
			} );

			it( title = "the administrator list only contains administrator caches", body = function( currentSpec ) {
				var adminName = "ldev2951admin";
				createAdminCache( adminName, 111 );
				try {
					var data = request( "list.cfm" );
					expect( data.adminNames ).toInclude( adminName );
					expect( data.adminNames ).notToInclude( "ldev2951app" );
					expect( data.adminNames ).notToInclude( "ldev2951shared" );
				}
				finally {
					removeAdminCache( adminName );
				}
			} );

			it( title = "the error for an unknown cache lists the application caches", body = function( currentSpec ) {
				var data = request( "list.cfm" );
				expect( data.message ).toInclude( "ldev2951app" );
			} );

			it( title = "a request without these application caches does not see them", body = function( currentSpec ) {
				var conns = getPageContext().getConfig().getCacheConnections();
				expect( conns.containsKey( "ldev2951app" ) ).toBeFalse();
			} );

			it( title = "the application cache can be used", body = function( currentSpec ) {
				var data = request( "list.cfm" );
				expect( data.value ).toBe( "app value" );
			} );

		} );
	}

	private struct function request( required string page ) {
		var uri = createURI( "LDEV2951/" & arguments.page );
		var result = _InternalRequest( template: uri );
		return deserializeJSON( trim( result.filecontent ) );
	}

	private void function createAdminCache( required string name, required numeric ttl ) {
		admin
			action="updateCacheConnection"
			type="server"
			password=server.SERVERADMINPASSWORD
			class="lucee.runtime.cache.ram.RamCache"
			name=arguments.name
			custom={ "timeToIdleSeconds": 0, "timeToLiveSeconds": arguments.ttl }
			default=""
			readonly=false
			storage=false
			remoteClients="";
	}

	private void function removeAdminCache( required string name ) {
		admin
			action="removeCacheConnection"
			type="server"
			password=server.SERVERADMINPASSWORD
			name=arguments.name;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
