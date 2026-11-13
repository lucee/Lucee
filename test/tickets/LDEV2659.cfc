component extends="org.lucee.cfml.test.LuceeTestCase" labels="catch" {

	function run( testResults, testBox ) {
		describe( "LDEV-2659 cfcatch name attribute with an explicit scope", function() {

			it( title="stores the exception in the given scope", body=function() {
				var res = deserializeJSON( _internalRequest( template: createURI( "LDEV2659/scoped.cfm" ) ).filecontent.trim() );

				expect( res.inVariables ).toBeTrue( "name=""Variables.ThisError"" must set variables.ThisError" );
				expect( res.weirdKey ).toBeFalse( "must not create a key named ""Variables.ThisError""" );
				expect( res.inRequest ).toBeTrue( "name=""request.ThisError"" must set request.ThisError" );
				expect( res.fn.inLocal ).toBeTrue( "name=""local.ex"" must set local.ex" );
				expect( res.fn.weirdKey ).toBeFalse( "must not create a key named ""local.ex""" );
			});

			it( title="a plain name still works", body=function() {
				var res = deserializeJSON( _internalRequest( template: createURI( "LDEV2659/scoped.cfm" ) ).filecontent.trim() );
				expect( res.plain ).toBeTrue();
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
