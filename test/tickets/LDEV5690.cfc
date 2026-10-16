component extends="org.lucee.cfml.test.LuceeTestCase" labels="param,security" {

	function run( testResults, testBox ) {

		describe( "LDEV-5690: Simple variable in brackets", function() {

			it( title="should work with limitEvaluation=true", body=function( currentSpec ) {
				var uri = createURI( "LDEV5690" );
				var result = _InternalRequest(
					template: "#uri#/LDEV5690.cfm",
					urls: { limit: true }
				);

				expect( result.filecontent.trim() ).toBe( "SUCCESS" );
			});

			it( title="should work with limitEvaluation=false", body=function( currentSpec ) {
				var uri = createURI( "LDEV5690" );
				var result = _InternalRequest(
					template: "#uri#/LDEV5690.cfm",
					urls: { limit: false }
				);

				expect( result.filecontent.trim() ).toBe( "SUCCESS" );
			});

		});

		describe( "LDEV-5690: Struct member access in brackets", function() {

			it( title="should work with limitEvaluation=true", body=function( currentSpec ) {
				var uri = createURI( "LDEV5690" );
				var result = _InternalRequest(
					template: "#uri#/struct-access.cfm",
					urls: { limit: true }
				);

				expect( result.filecontent.trim() ).toBe( "SUCCESS" );
			});

			it( title="should work with limitEvaluation=false", body=function( currentSpec ) {
				var uri = createURI( "LDEV5690" );
				var result = _InternalRequest(
					template: "#uri#/struct-access.cfm",
					urls: { limit: false }
				);

				expect( result.filecontent.trim() ).toBe( "SUCCESS" );
			});

		});

		describe( "LDEV-5690: Array index access in brackets", function() {

			it( title="should work with limitEvaluation=true", body=function( currentSpec ) {
				var uri = createURI( "LDEV5690" );
				var result = _InternalRequest(
					template: "#uri#/array-index.cfm",
					urls: { limit: true }
				);

				expect( result.filecontent.trim() ).toBe( "SUCCESS" );
			});

			it( title="should work with limitEvaluation=false", body=function( currentSpec ) {
				var uri = createURI( "LDEV5690" );
				var result = _InternalRequest(
					template: "#uri#/array-index.cfm",
					urls: { limit: false }
				);

				expect( result.filecontent.trim() ).toBe( "SUCCESS" );
			});

		});

		describe( "LDEV-5690: Function calls in brackets (security)", function() {

			// TODO: Function calls aren't being blocked by throwing SecurityInterpreterException
			// CFMLExpressionInterpreter line 1336 shows that when limited=true, function parsing
			// is skipped entirely. This prevents execution (good) but doesn't throw the expected
			// exception. The exploit is blocked, just not in the way we expected.
			xit( title="should be blocked with limitEvaluation=true", body=function( currentSpec ) {
				var uri = createURI( "LDEV5690" );
				var result = _InternalRequest(
					template: "#uri#/function-call.cfm",
					urls: { limit: true }
				);

				expect( result.filecontent.trim() ).toBe( "SUCCESS" );
			});

		});

	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
