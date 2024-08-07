component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV2374");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-2374, mergeFormUrlAsStruct=false", function() {
			it( title='Checking url scope with multiple dot notation [&ts=10&res...temp=1]', body=function( currentSpec ) {
				var qs = "ts=10&res...temp=1";
				local.result = _InternalRequest(
					template : "#uri#/LDEV2374.cfm",
					urls: qs
				);
				var parsedUrl = deserializeJSON( result.filecontent ); 

				systemOutput( "", true );
				systemOutput( qs, true );
				systemOutput( parsedUrl, true );

				expect( structCount( parsedUrl ) ).toBe( 2 );
				expect( parsedUrl ).toHaveKey( "ts" );
				expect( parsedUrl ).notToHaveKey( "res" );
				expect( parsedUrl ).toHaveKey( "res...temp" );
			});

			it( title='Checking url scope with single dot notation [&mt=10&test.temp=lucee]', body=function( currentSpec ) {
				var qs = "mt=10&test.temp=lucee";
				local.result = _InternalRequest(
					template : "#uri#/LDEV2374.cfm",
					urls: qs
				);

				var parsedUrl = deserializeJSON( result.filecontent ); 

				systemOutput( "", true );
				systemOutput( qs, true );
				systemOutput( parsedUrl, true );

				expect( structCount( parsedUrl ) ).toBe( 2 );
				expect( parsedUrl ).toHaveKey( "mt" );
				expect( parsedUrl ).toHaveKey( "test.temp" );
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

} 
