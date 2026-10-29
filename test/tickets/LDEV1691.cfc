component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi" {

	variables.val = "<b>&'""";

	function run( testResults, textbox ) {
		describe( "LDEV-1691 application wide default for the encodeFor of cfoutput", function() {

			it( title="no default, nothing is encoded", body=function( currentSpec ) {
				expect( req( "plain.cfm" ) ).toBe( "[" & val & "]" );
			});

			it( title="this.encodeFor=html encodes cfoutput, text of the template is untouched", body=function( currentSpec ) {
				expect( req( "plain.cfm", "html" ) ).toBe( "[" & encodeForHTML( val ) & "]" );
				expect( req( "plain.cfm", "html" ) ).notToBe( "[" & val & "]" );
			});

			it( title="the default can be any encoding type", body=function( currentSpec ) {
				expect( req( "plain.cfm", "url" ) ).toBe( "[" & encodeForURL( val ) & "]" );
				expect( req( "plain.cfm", "javascript" ) ).toBe( "[" & encodeForJavaScript( val ) & "]" );
			});

			it( title="the type is not case sensitive and is trimmed", body=function( currentSpec ) {
				expect( req( "plain.cfm", " HTML " ) ).toBe( "[" & encodeForHTML( val ) & "]" );
			});

			it( title="empty this.encodeFor is the same as not set", body=function( currentSpec ) {
				expect( req( "plain.cfm", "" ) ).toBe( "[" & val & "]" );
			});

			it( title="encodeFor attribute of the cfoutput wins over the default", body=function( currentSpec ) {
				expect( req( "explicit.cfm", "html" ) ).toBe( encodeForURL( val ) );
			});

			it( title="output that is already encoded is not encoded twice", body=function( currentSpec ) {
				expect( req( "already.cfm", "html" ) ).toBe( encodeForHTML( val ) );
			});

			it( title="cfoutput in a cfincluded template uses the default of the application", body=function( currentSpec ) {
				expect( req( "include.cfm", "html" ) ).toBe( encodeForURL( val ) & "|" & encodeForHTML( val ) );
			});

			it( title="encodeFor of the surrounding cfoutput is not passed to a cfincluded cfoutput", body=function( currentSpec ) {
				expect( req( "include.cfm" ) ).toBe( encodeForURL( val ) & "|" & val );
			});

			it( title="works with cfoutput query", body=function( currentSpec ) {
				expect( req( "query.cfm" ) ).toBe( val & ";x;" );
				expect( req( "query.cfm", "html" ) ).toBe( encodeForHTML( val ) & ";x;" );
			});

			it( title="output of cfscript is not affected", body=function( currentSpec ) {
				expect( req( "script.cfm", "html" ) ).toBe( val );
			});

			it( title="getApplicationSettings returns the default", body=function( currentSpec ) {
				expect( req( "settings.cfm", "html" ) ).toBe( "html" );
				expect( req( "settings.cfm" ) ).toBe( "" );
			});

			it( title="an invalid type fails when something is output", body=function( currentSpec ) {
				expect( function() {
					req( "plain.cfm", "susi" );
				}).toThrow();
			});

			it( title="attribute encodeFor of the tag cfapplication", body=function( currentSpec ) {
				expect( req( "tag.cfm" ) ).toBe( encodeForHTML( val ) );
			});

		});
	}

	private string function req( required string page, string enc ) {
		var args = { v: val };
		if ( !isNull( arguments.enc ) ) args.enc = arguments.enc;
		var result = _InternalRequest(
			template: "/test/#listLast( getDirectoryFromPath( getCurrentTemplatepath() ), "\/" )#/LDEV1691/#arguments.page#",
			url: args
		);
		return result.filecontent.trim();
	}

}
