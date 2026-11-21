component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	// LDEV-6938: merging the local storage scope with a newer copy in storage dropped values written locally

	function run( testResults, testBox ) {
		describe( "LDEV-6938: storage scope merge keeps values written locally", function() {

			it( "a scope created without storage keeps a key that is missing in storage", function() {
				var localData = toData( { foo: "local" }, 100 );
				var storageData = toData( { bar: "stored" }, 50 );
				merge( localData, storageData, 200, 0 );
				expect( valueOf( storageData, "foo" ) ).toBe( "local" );
				expect( valueOf( storageData, "bar" ) ).toBe( "stored" );
			});

			it( "a key written locally after the stored copy of it wins, even if storage changed since", function() {
				var localData = toData( { foo: "local" }, 100 );
				var storageData = toData( { foo: "stored" }, 50 );
				merge( localData, storageData, 200, 10 );
				expect( valueOf( storageData, "foo" ) ).toBe( "local" );
			});

			it( "a key written to storage after the local write wins", function() {
				var localData = toData( { foo: "local" }, 100 );
				var storageData = toData( { foo: "stored" }, 150 );
				merge( localData, storageData, 200, 10 );
				expect( valueOf( storageData, "foo" ) ).toBe( "stored" );
			});

			it( "a key loaded from storage and removed there is not added back", function() {
				var localData = toData( { foo: "local" }, 5 );
				var storageData = toData( { bar: "stored" }, 50 );
				merge( localData, storageData, 200, 10 );
				expect( storageData.containsKey( key( "foo" ) ) ).toBeFalse();
			});
		});
	}

	// a Map<Key,IKStorageScopeItem> as the storage scopes hold it, every entry with the given timestamp
	private function toData( struct sct, numeric lastModified ) {
		arguments.sct.lastvisit = createObject( "java", "java.util.Date" ).init( javaCast( "long", arguments.lastModified ) );
		return createObject( "java", "lucee.runtime.type.scope.storage.IKHandlerDatasource" ).toIKStorageValue( arguments.sct ).getValue();
	}

	private function merge( localData, storageData, numeric storageLastModified, numeric lastModifiedAtInit ) {
		var Scope = createObject( "java", "lucee.runtime.type.scope.Scope" );
		createObject( "java", "lucee.runtime.type.scope.storage.IKStorageScopeSupport" ).merge( arguments.localData, arguments.storageData, nullValue(),
			Scope.SCOPE_SESSION, javaCast( "long", arguments.storageLastModified ), javaCast( "long", arguments.lastModifiedAtInit ) );
	}

	private function valueOf( data, string name ) {
		return arguments.data.get( key( arguments.name ) );
	}

	private function key( string name ) {
		return createObject( "java", "lucee.runtime.type.KeyImpl" ).getInstance( arguments.name );
	}
}
