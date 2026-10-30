component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-2951 unknown cache error lists the application caches", function() {

			it( title = "the error for an unknown cache names the application cache and the administrator caches", body = function( currentSpec ) {
				var adminName = "ldev2951admin";
				createAdminCache( adminName );
				try {
					var data = request( "list.cfm" );
					expect( data.message ).toInclude( "no cache defined with name [ldev2951-no-such-cache]" );
					expect( data.message ).toInclude( "ldev2951app" );
					expect( data.message ).toInclude( adminName );
				}
				finally {
					removeAdminCache( adminName );
				}
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

	private void function createAdminCache( required string name ) {
		admin
			action="updateCacheConnection"
			type="server"
			password=server.SERVERADMINPASSWORD
			class="lucee.runtime.cache.ram.RamCache"
			name=arguments.name
			custom={ "timeToIdleSeconds": 0, "timeToLiveSeconds": 111 }
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
