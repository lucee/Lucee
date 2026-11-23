component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV5685" {

	function beforeAll() {
		variables.uri = createURI( "LDEV5685" );
	}

	private struct function call( string dir ) {
		var res = _internalRequest(
			template: "#variables.uri#/#arguments.dir#/index.cfm",
			urls: "a.b=1",
			forms: { "f.g": "2" }
		);
		return deserializeJSON( res.fileContent );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5685 cfapplication formUrlAsStruct", function() {

			it( title="<cfapplication formUrlAsStruct=false> keeps dotted keys", body=function( currentSpec ) {
				var r = call( "off" );
				expect( r.setting ).toBeFalse();
				expect( r.urlIsStruct ).toBeFalse();
				expect( listFindNoCase( r.urlKeys, "a.b" ) ).toBeGT( 0 );
				expect( r.formIsStruct ).toBeFalse();
				expect( listFindNoCase( r.formKeys, "f.g" ) ).toBeGT( 0 );
			});

			it( title="<cfapplication formUrlAsStruct=true> builds structs", body=function( currentSpec ) {
				var r = call( "on" );
				expect( r.setting ).toBeTrue();
				expect( r.urlIsStruct ).toBeTrue();
				expect( r.formIsStruct ).toBeTrue();
			});

			it( title="script application formUrlAsStruct=false", body=function( currentSpec ) {
				var r = call( "script" );
				expect( r.setting ).toBeFalse();
				expect( r.urlIsStruct ).toBeFalse();
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
