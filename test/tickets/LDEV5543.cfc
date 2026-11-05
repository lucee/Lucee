component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function beforeAll(){
		variables.testUrl = "https://update.lucee.org/rest/update/provider/light/5.3.10.97"; // returns a 302 to lucee cdn
	}

	function run( testResults, testBox ){

		describe( "LDEV-5543", function(){

			it( "test VALID http tag in script syntax", function(){
				http
					username="zac"
					result="local.result"
					method = "get"
					url = "#testUrl#"
					redirect = "yes";
				expect ( local ).toHaveKey( "result" );
				expect ( isStruct( local.result ) ).toBeTrue();
			});

			it( "test BAD http tag in script syntax", function(){
				expect( function(){
					_InternalRequest( template: createURI( "LDEV5543/bad_http.cfm" ) );
				} ).toThrow( regex="Attribute \[usename\] is not allowed for statement \[http\]" );
			});

			it( "test variable named like a tag (regression check)", function(){
				query = "marker=test";
				query &= "&extra=1";
				expect( query ).toBe( "marker=test&extra=1" );

				http = "https://lucee.org";
				http &= "/update";
				expect( http ).toBe( "https://lucee.org/update" );
			});

		});
	}

	private string function createURI( required string calledName ){
		return "/test/tickets/" & calledName;
	}
}
