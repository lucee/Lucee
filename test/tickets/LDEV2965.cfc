component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI( "LDEV2965" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-2965", function() {

			it( title="remote function with returnformat plain has no trailing newline", body=function() {
				var res = _InternalRequest(
					template: "#variables.uri#/Service.cfc",
					url: { method: "getFileName" }
				);
				expect( res.fileContent ).toBe( "report.txt" );
				expect( res.fileContent.len() ).toBe( len( "report.txt" ) );

				var resUrl = _InternalRequest(
					template: "#variables.uri#/Service.cfc",
					url: { method: "getFileName", returnformat: "plain" }
				);
				expect( resUrl.fileContent ).toBe( "report.txt" );

				var resLine = _InternalRequest(
					template: "#variables.uri#/Service.cfc",
					url: { method: "getLine" }
				);
				expect( resLine.fileContent ).toBe( "line" & chr( 10 ) );
			});

			it( title="remote proxy lists remote functions", body=function() {
				var res = _InternalRequest(
					template: "#variables.uri#/Service.cfc",
					url: "json"
				);
				var meta = deserializeJSON( res.fileContent );
				expect( meta ).toHaveKey( "functions" );
				expect( meta.functions ).toHaveKey( "getFileName" );
				expect( meta.functions ).toHaveKey( "getLine" );
				expect( meta.functions ).toHaveKey( "ping" );
				expect( meta.functions ).notToHaveKey( "sharedFn" );
				expect( meta.functions ).notToHaveKey( "packageFn" );
				expect( meta.functions ).notToHaveKey( "localFn" );

				var child = _InternalRequest(
					template: "#variables.uri#/ChildService.cfc",
					url: "json"
				);
				var childMeta = deserializeJSON( child.fileContent );
				expect( childMeta.functions ).toHaveKey( "childPing" );
				expect( childMeta.functions ).toHaveKey( "ping" );
				expect( childMeta.functions ).notToHaveKey( "sharedFn" );
				expect( childMeta.functions ).notToHaveKey( "localFn" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
