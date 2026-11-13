component extends="org.lucee.cfml.test.LuceeTestCase" labels="form,url" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6889" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6889 trailing [] form and URL fields are arrays", function() {

			it( "returns a one-element array for a single url tags[] value", function() {
				assertArrayField( "url", "tags[]=lucee", "url|tags", [ "lucee" ] );
			});

			it( "returns a one-element array for a single form tags[] value", function() {
				assertArrayField( "form", "tags[]=lucee", "form|tags", [ "lucee" ] );
			});

			it( "returns an array of two values for repeated url tags[] fields", function() {
				assertArrayField( "url", "tags[]=java&tags[]=cfml", "url|tags", [ "java", "cfml" ] );
			});

			it( "returns an array of two values for repeated form tags[] fields", function() {
				assertArrayField( "form", "tags[]=java&tags[]=cfml", "form|tags", [ "java", "cfml" ] );
			});

			it( "returns a nested array for a single url user[tags][] value", function() {
				assertArrayField( "url", "user[tags][]=lucee", "url|user|tags", [ "lucee" ] );
			});

			it( "returns a nested array for a single form user[tags][] value", function() {
				assertArrayField( "form", "user[tags][]=lucee", "form|user|tags", [ "lucee" ] );
			});

			it( "returns a nested array for repeated url user[tags][] fields", function() {
				assertArrayField( "url", "user[tags][]=foo&user[tags][]=bar", "url|user|tags", [ "foo", "bar" ] );
			});

			it( "returns a nested array for repeated form user[tags][] fields", function() {
				assertArrayField( "form", "user[tags][]=foo&user[tags][]=bar", "form|user|tags", [ "foo", "bar" ] );
			});

			it( "returns a nested array for a single url user.tags[] value", function() {
				assertArrayField( "url", "user.tags[]=lucee", "url|user|tags", [ "lucee" ] );
			});

			it( "returns a nested array for a single form user.tags[] value", function() {
				assertArrayField( "form", "user.tags[]=lucee", "form|user|tags", [ "lucee" ] );
			});

			it( "returns a nested array for repeated url user.tags[] fields", function() {
				assertArrayField( "url", "user.tags[]=foo&user.tags[]=bar", "url|user|tags", [ "foo", "bar" ] );
			});

			it( "returns a nested array for repeated form user.tags[] fields", function() {
				assertArrayField( "form", "user.tags[]=foo&user.tags[]=bar", "form|user|tags", [ "foo", "bar" ] );
			});

		});
	}

	private void function assertArrayField( required string scope, required string query, required string path, required array values ) {
		var report = requestScope( arguments.scope, arguments.query );
		expect( report ).toHaveKey( arguments.path );
		var entry = report[ arguments.path ];
		expect( entry.type ).toBe( "array" );
		expect( arrayLen( entry.value ) ).toBe( arrayLen( arguments.values ) );
		for ( var i = 1; i <= arrayLen( arguments.values ); i++ ) {
			expect( entry.value[ i ] ).toBe( arguments.values[ i ] );
		}
	}

	private struct function requestScope( required string scope, required string query ) {
		var result = "";
		if ( arguments.scope == "url" ) {
			result = _internalRequest(
				template: "#variables.uri#/test.cfm",
				urls: arguments.query
			);
		}
		else {
			result = _internalRequest(
				template: "#variables.uri#/test.cfm",
				forms: arguments.query
			);
		}
		return parseReport( result.filecontent );
	}

	private struct function parseReport( required string content ) {
		var report = {};
		var lines = listToArray( arguments.content, chr( 10 ), false );
		for ( var line in lines ) {
			if ( !len( trim( line ) ) ) continue;
			var eq = find( "=", line );
			if ( !eq ) throw( message = "Unparseable scope report line: " & line );
			var path = left( line, eq - 1 );
			var rest = mid( line, eq + 1, len( line ) );
			var colon = find( ":", rest );
			if ( !colon ) throw( message = "Unparseable scope report line: " & line );
			var type = left( rest, colon - 1 );
			var payload = mid( rest, colon + 1, len( rest ) );
			var entry = { "type": type };
			if ( type == "array" ) entry.value = deserializeJSON( payload );
			else entry.value = payload;
			report[ path ] = entry;
		}
		return report;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
