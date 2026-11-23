component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	// LDEV-6939: storage scopes detect changes by hashing their values, components hash by identity,
	// so a component property changed via its accessor was never stored

	function run( testResults, testBox ) {
		describe( "LDEV-6939: storage scope change detection for components", function() {

			it( "a property changed via its accessor is written to storage", function() {
				var uri = createURI( "LDEV6939" );
				var first = _InternalRequest( template: "#uri#/modify.cfm", url: { username: "" } );
				var ids = deserializeJSON( first.fileContent );
				var cookies = { cfid: ids.cfid, cftoken: ids.cftoken };
				_InternalRequest( template: "#uri#/modify.cfm", url: { username: "zac" }, cookies: cookies );
				var stored = _InternalRequest( template: "#uri#/stored.cfm", cookies: cookies );
				expect( trim( stored.fileContent ) ).toBe( "zac" );
			});

			it( "detects a property changed via its accessor", function() {
				var user = new LDEV6939.User();
				var data = toData( { user: user } );
				var before = hashOf( data, "user" );
				user.setUsername( "zac" );
				expect( hashOf( data, "user" ) ).notToBe( before );
			});

			it( "detects a change in the this scope", function() {
				var user = new LDEV6939.User();
				var data = toData( { user: user } );
				var before = hashOf( data, "user" );
				user.nickname = "z";
				expect( hashOf( data, "user" ) ).notToBe( before );
			});

			it( "detects a change in a struct held by the component", function() {
				var user = new LDEV6939.User();
				var data = toData( { user: user } );
				var before = hashOf( data, "user" );
				user.getPrefs().theme = "dark";
				expect( hashOf( data, "user" ) ).notToBe( before );
			});

			it( "detects a change in a component nested in a struct", function() {
				var user = new LDEV6939.User();
				var data = toData( { holder: { user: user } } );
				var before = hashOf( data, "holder" );
				user.setRole( "admin" );
				expect( hashOf( data, "holder" ) ).notToBe( before );
			});

			it( "keeps the hash when nothing changed", function() {
				var user = new LDEV6939.User();
				var data = toData( { user: user } );
				var before = hashOf( data, "user" );
				user.getUsername();
				expect( hashOf( data, "user" ) ).toBe( before );
			});

			it( "handles a component that references itself", function() {
				var user = new LDEV6939.User();
				user.setSelf();
				var data = toData( { user: user } );
				var before = hashOf( data, "user" );
				user.setUsername( "zac" );
				expect( hashOf( data, "user" ) ).notToBe( before );
			});
		});
	}

	// a Map<Key,IKStorageScopeItem> as the storage scopes hold it
	private function toData( struct sct ) {
		return createObject( "java", "lucee.runtime.type.scope.storage.IKHandlerDatasource" ).toIKStorageValue( arguments.sct ).getValue();
	}

	private numeric function hashOf( data, string name ) {
		var Scope = createObject( "java", "lucee.runtime.type.scope.Scope" );
		var hashes = createObject( "java", "lucee.runtime.type.scope.ScopeContext" ).hashes( arguments.data, Scope.SCOPE_SESSION, true );
		return hashes.get( createObject( "java", "lucee.runtime.type.KeyImpl" ).getInstance( arguments.name ) );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}
}
