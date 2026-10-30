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

			it( title="encodeFor=none of the cfoutput outputs as it is, even with this.encodeFor set", body=function( currentSpec ) {
				expect( req( "none.cfm", "html" ) ).toBe( "[" & val & "]" );
				expect( req( "none.cfm", "url" ) ).toBe( "[" & val & "]" );
			});

			it( title="encodeFor=none is not case sensitive", body=function( currentSpec ) {
				expect( req( "none-case.cfm", "html" ) ).toBe( "[" & val & "]" );
			});

			it( title="encodeFor=none of the cfoutput with no default for the application", body=function( currentSpec ) {
				expect( req( "none.cfm" ) ).toBe( "[" & val & "]" );
				expect( req( "none.cfm", "" ) ).toBe( "[" & val & "]" );
				expect( req( "none.cfm", "none" ) ).toBe( "[" & val & "]" );
			});

			it( title="encodeFor=none can be a dynamic value", body=function( currentSpec ) {
				expect( req( "dynamic.cfm", "html", "none" ) ).toBe( "[" & val & "]" );
				expect( req( "dynamic.cfm", "html", "NONE" ) ).toBe( "[" & val & "]" );
				expect( req( "dynamic.cfm", "html", "url" ) ).toBe( "[" & encodeForURL( val ) & "]" );
			});

			it( title="empty encodeFor of the cfoutput outputs as it is, even with this.encodeFor set", body=function( currentSpec ) {
				expect( req( "empty.cfm", "html" ) ).toBe( "[" & val & "]" );
				expect( req( "empty.cfm" ) ).toBe( "[" & val & "]" );
			});

			it( title="encodeFor=none only affects its own cfoutput", body=function( currentSpec ) {
				expect( req( "mixed.cfm", "html" ) ).toBe( encodeForHTML( val ) & "|" & val & "|" & encodeForHTML( val ) );
				expect( req( "mixed.cfm" ) ).toBe( val & "|" & val & "|" & val );
			});

			it( title="encodeFor=none is not passed to a cfincluded cfoutput, that one uses the default", body=function( currentSpec ) {
				expect( req( "include-none.cfm", "html" ) ).toBe( val & "|" & encodeForHTML( val ) );
				expect( req( "include-none.cfm" ) ).toBe( val & "|" & val );
			});

			it( title="encodeFor=none works with cfoutput query", body=function( currentSpec ) {
				expect( req( "query-none.cfm", "html" ) ).toBe( val & ";x;" );
			});

			it( title="this.encodeFor=none is the same as not set", body=function( currentSpec ) {
				expect( req( "plain.cfm", "none" ) ).toBe( "[" & val & "]" );
				expect( req( "plain.cfm", " NONE " ) ).toBe( "[" & val & "]" );
				expect( req( "explicit.cfm", "none" ) ).toBe( encodeForURL( val ) );
				expect( req( "settings.cfm", "none" ) ).toBe( "" );
			});

			it( title="attribute encodeFor=none of the tag cfapplication is the same as not set", body=function( currentSpec ) {
				expect( req( "tag-none.cfm", "html" ) ).toBe( val );
			});

			it( title="attribute encodeFor of the tag cfapplication", body=function( currentSpec ) {
				expect( req( "tag.cfm" ) ).toBe( encodeForHTML( val ) );
			});

		});
	}

	private string function req( required string page, string enc, string type ) {
		var args = { v: val };
		if ( !isNull( arguments.enc ) ) args.enc = arguments.enc;
		if ( !isNull( arguments.type ) ) args.t = arguments.type;
		var result = _InternalRequest(
			template: "/test/#listLast( getDirectoryFromPath( getCurrentTemplatepath() ), "\/" )#/LDEV1691/#arguments.page#",
			url: args
		);
		return result.filecontent.trim();
	}

}
