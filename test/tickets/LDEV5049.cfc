component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV5049");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-5049", function() {

			it( title='internalRequest string url scope with leading & [&mt=10]', body=function( currentSpec ) {
				var qs = "&mt=10";
				local.result = _InternalRequest(
					template : "#uri#/LDEV5049_url.cfm",
					urls: qs
				);

				var parsedUrl = deserializeJSON( result.filecontent ); 

				systemOutput( "", true );
				systemOutput( qs, true );
				systemOutput( parsedUrl, true );

				expect( parsedUrl ).toHaveKey( "mt" );
				expect( structCount( parsedUrl ) ).toBe( 1 );
			});

			it( title='internalRequest string url scope with trailing & [mt=10&]', body=function( currentSpec ) {
				var qs = "mt=10&";
				local.result = _InternalRequest(
					template : "#uri#/LDEV5049_url.cfm",
					urls: qs
				);

				var parsedUrl = deserializeJSON( result.filecontent ); 

				systemOutput( "", true );
				systemOutput( qs, true );
				systemOutput( parsedUrl, true );

				expect( parsedUrl ).toHaveKey( "mt" );
				expect( structCount( parsedUrl ) ).toBe( 1 );
			});

			it( title='internalRequest string form scope with leading & [&mt=10]', body=function( currentSpec ) {
				var qs = "&mt=10";
				local.result = _InternalRequest(
					template : "#uri#/LDEV5049_form.cfm",
					form: qs
				);

				var parsedForm = deserializeJSON( result.filecontent ); 

				systemOutput( "", true );
				systemOutput( qs, true );
				systemOutput( parsedForm, true );

				expect( parsedForm ).toHaveKey( "mt" );
				expect( parsedForm ).toHaveKey( "fieldnames" );
				expect( structCount( parsedForm ) ).toBe( 2 );
			});

			it( title='internalRequest string form scope with trailing & [mt=10&]', body=function( currentSpec ) {
				var qs = "mt=10&";
				local.result = _InternalRequest(
					template : "#uri#/LDEV5049_form.cfm",
					form: qs
				);

				var parsedForm = deserializeJSON( result.filecontent ); 

				systemOutput( "", true );
				systemOutput( qs, true );
				systemOutput( parsedForm, true );

				expect( parsedForm ).toHaveKey( "mt" );
				expect( parsedForm ).toHaveKey( "fieldnames" );
				expect( structCount( parsedForm ) ).toBe( 2 );
			});

			it( title='internalRequest string url [a&=b]', body=function( currentSpec ) {
				var qs = "a&=b";
				local.result = _InternalRequest(
					template : "#uri#/LDEV5049_url.cfm",
					urls: qs
				);

				var parsedUrl = deserializeJSON( result.filecontent ); 

				systemOutput( "", true );
				systemOutput( qs, true );
				systemOutput( parsedUrl, true );

				expect( parsedUrl ).toHaveKey( "a" );
				expect( parsedUrl["a"] ).toBe( "" );
				expect( parsedUrl ).toHaveKey( "" );
				expect( parsedUrl[""] ).toBe( "b" );
				expect( structCount( parsedUrl ) ).toBe( 2 );
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

} 
