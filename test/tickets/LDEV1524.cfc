component extends="org.lucee.cfml.test.LuceeTestCase" labels="whitespace" {

	function run( testResults, testBox ) {
		describe( "LDEV-1524 light whitespace management", function() {

			var NL = chr( 10 );

			describe( "this.whitespaceManagement = ""white-space-light"" in Application.cfc", function() {

				it( title="removes empty lines and trailing whitespace, keeps indentation and whitespace within a line", body=function( currentSpec ) {
					expect( req( "light/page.cfm" ) ).toBe(
						"<div>" & NL
						& "    <p>hello   world</p>" & NL
						& "    <ul>" & NL
						& "        <li>a</li>" & NL
						& "    </ul>" & NL
						& "</div>" & NL
						& "<PRE>" & NL
						& "  keep   this  " & NL & NL
						& "  too   " & NL
						& "</PRE>" & NL
						& "<textarea rows=""2"">  a  " & NL & NL
						& "  b  </textarea>" & NL
						& "<code>  c  " & NL
						& "</code>" );
				});

				it( title="handles CRLF, tags written in several parts and numbers", body=function( currentSpec ) {
					expect( req( "light/script.cfm" ) ).toBe(
						"a" & NL
						& "  b" & NL
						& "<pre>  x  " & NL & NL & "  y</pre>" & NL
						& "  z  42" & NL
						& "<span>" & chr( 160 ) & "</span>" & NL
						& "end" );
				});

				it( title="works across a flush", body=function( currentSpec ) {
					expect( req( "light/flush.cfm" ) ).toBe( "a" & NL & "  b" & NL & "  c" & NL & "d" );
				});

				it( title="savecontent trim works like for the other whitespace management modes", body=function( currentSpec ) {
					expect( req( "light/savecontent.cfm" ) ).toBe( "[x    y]" );
				});

				it( title="the value ""light"" is an alias", body=function( currentSpec ) {
					expect( req( "alias/page.cfm" ) ).toBe( "a    b" & NL & "   c  d" );
				});

				it( title="the setting is returned by getApplicationSettings", body=function( currentSpec ) {
					expect( req( "light/settings.cfm" ) ).toBe( "white-space-light" );
					expect( req( "alias/settings.cfm" ) ).toBe( "white-space-light" );
				});

				it( title="an invalid value throws an error", body=function( currentSpec ) {
					expect( function() {
						req( "invalid/invalid.cfm" );
					}).toThrow();
				});

			});

			describe( "cfsetting whitespaceManagement per request", function() {

				it( title="switch from regular to light for the rest of the request", body=function( currentSpec ) {
					var expected = "a   " & NL & NL & "  b" & NL & "  c" & NL & "  d";
					expect( req( "regular/request.cfm", { mode: "white-space-light" } ) ).toBe( expected );
					expect( req( "regular/request.cfm", { mode: "light" } ) ).toBe( expected );
				});

				it( title="switch from light to regular for the rest of the request", body=function( currentSpec ) {
					expect( req( "light/back.cfm" ) ).toBe( "a" & NL & "  b" & NL & "c   " & NL & NL & "  d" );
				});

			});

		});
	}

	private string function req( required string page, struct url={} ) {
		// the result is not trimmed, as the leading and trailing whitespace is what is tested
		var res = _InternalRequest( template: createURI( "LDEV1524/" & arguments.page ), url: arguments.url ).fileContent;
		return reReplace( res, "[\r\n]+$", "" );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
