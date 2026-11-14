component extends="org.lucee.cfml.test.LuceeTestCase" labels="error" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6142" );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6142 detailed error template returns markdown when Accept prefers text/markdown", body=function() {

			describe( title="markdown output", body=function() {

				it( title="sets the markdown content type and keeps the error status", body=function() {
					var req = doRequest( "text/markdown" );
					expect( req.status_code ).toBe( 500 );
					expect( req.headers[ "Content-Type" ] ).toBe( "text/markdown; charset=utf-8" );
				});

				it( title="contains type, message, detail and extended info", body=function() {
					var md = doRequest( "text/markdown" ).filecontent;
					expect( md ).toInclude( heading( 2, "Lucee #server.lucee.version# Error (LDEV6142.custom)" ) );
					expect( md ).toInclude( heading( 3, "Message" ) & chr( 10 ) & chr( 10 ) & "LDEV-6142 message" );
					expect( md ).toInclude( heading( 3, "Detail" ) & chr( 10 ) & chr( 10 ) & "LDEV-6142 detail" );
					expect( md ).toInclude( heading( 3, "Extended Info" ) & chr( 10 ) & chr( 10 ) & "LDEV-6142 extended info" );
				});

				it( title="contains the tag context with template, line and a fenced code snippet", body=function() {
					var md = doRequest( "text/markdown" ).filecontent;
					expect( md ).toInclude( heading( 3, "Stacktrace" ) );
					expect( md ).toInclude( "**#expandPath( uri & "/throw.cfm" )#: line 1**" );
					// the fixture is checked out with the platform line endings, keep only the line text
					var firstLine = replace( listFirst( fileRead( expandPath( uri & "/throw.cfm" ) ), chr( 10 ) ), chr( 13 ), "", "all" );
					expect( md ).toInclude( "```" & chr( 10 ) & "1: " & firstLine );
				});

				it( title="lists the calling templates of a nested include", body=function() {
					var md = doRequest( "text/markdown", "/nested.cfm" ).filecontent;
					expect( md ).toInclude( "**#expandPath( uri & "/throw.cfm" )#: line 1**" );
					expect( md ).toInclude( "**called from** #expandPath( uri & "/nested.cfm" )#: line 1" );
				});

				it( title="contains the additional entries as they are", body=function() {
					var md = doRequest( "text/markdown", "/missingInclude.cfm" ).filecontent;
					expect( md ).toInclude( heading( 3, "Mapping" ) & chr( 10 ) & chr( 10 ) );
				});

				it( title="contains the java stacktrace in a fenced block", body=function() {
					var md = doRequest( "text/markdown" ).filecontent;
					var start = find( heading( 3, "Java Stacktrace" ) & chr( 10 ) & chr( 10 ) & "```" & chr( 10 ), md );
					expect( start ).toBeGT( 0 );
					expect( md ).toInclude( "lucee.runtime.exp.CustomTypeException" );
					expect( md ).toInclude( chr( 10 ) & "```" & chr( 10 ) & chr( 10 ) & heading( 3, "Timestamp" ) );
				});

				it( title="does not contain any HTML, styles or script", body=function() {
					var md = doRequest( "text/markdown" ).filecontent;
					// the needles are assembled, so the code snippets of this file in the tag context can not contain them
					var lt = chr( 60 );
					expect( md ).notToInclude( lt & "table" );
					expect( md ).notToInclude( lt & "style" );
					expect( md ).notToInclude( lt & "script" );
					expect( md ).notToInclude( "-lucee" & "-err" );
					expect( md ).notToInclude( lt & "/TD>" );
				});

				it( title="does not contain more than the HTML detailed view", body=function() {
					var html = doRequest( "text/html" ).filecontent;
					var md = doRequest( "text/markdown" ).filecontent;
					// every section of the markdown has its counterpart in the HTML view
					var sections = reMatch( "(?m)^" & repeatString( chr( 35 ), 3 ) & " (.+)$", md );
					expect( arrayLen( sections ) ).toBeGT( 0 );
					loop array=sections item="local.section" {
						var label = trim( mid( section, 4 ) );
						expect( html ).toInclude( ">" & label );
					}
				});

			});

			describe( title="Accept header handling", body=function() {

				it( title="text/markdown as the only type returns markdown", body=function() {
					expectMarkdown( "text/markdown" );
				});

				it( title="text/markdown listed before text/html returns markdown", body=function() {
					expectMarkdown( "text/markdown, text/html" );
				});

				it( title="text/markdown with a higher q than text/html returns markdown", body=function() {
					expectMarkdown( "text/html;q=0.5, text/markdown;q=0.9" );
				});

				it( title="text/markdown together with a wildcard returns markdown", body=function() {
					expectMarkdown( "text/markdown, */*" );
					expectMarkdown( "text/markdown, */*;q=0.8" );
				});

				it( title="media type and parameters are case insensitive and may contain spaces", body=function() {
					expectMarkdown( " Text/Markdown ; Charset=utf-8 ; Q=0.9 , text/html ; q=0.1" );
				});

				it( title="text/html listed before text/markdown returns html", body=function() {
					expectHTML( "text/html, text/markdown" );
				});

				it( title="text/html with a higher q than text/markdown returns html", body=function() {
					expectHTML( "text/markdown;q=0.5, text/html" );
				});

				it( title="text/markdown with q=0 returns html", body=function() {
					expectHTML( "text/markdown;q=0" );
					expectHTML( "text/markdown;q=0, text/html" );
				});

				it( title="a wildcard alone returns html", body=function() {
					expectHTML( "*/*" );
					expectHTML( "text/*" );
				});

				it( title="a browser Accept header returns html", body=function() {
					expectHTML( "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8" );
				});

				it( title="other types only return html", body=function() {
					expectHTML( "application/json" );
				});

				it( title="no Accept header returns html", body=function() {
					var req = _InternalRequest( template: "#uri#/throw.cfm", throwonerror: false );
					expect( req.status_code ).toBe( 500 );
					expect( req.headers[ "Content-Type" ] ).toInclude( "text/html" );
					expect( req.filecontent ).toInclude( 'id="-lucee-err"' );
				});

			});

			describe( title="HTML output", body=function() {

				it( title="is unchanged and contains the error details", body=function() {
					var req = doRequest( "text/html" );
					expect( req.status_code ).toBe( 500 );
					expect( req.headers[ "Content-Type" ] ).toInclude( "text/html" );
					expect( req.filecontent ).toInclude( 'id="-lucee-err"' );
					expect( req.filecontent ).toInclude( "LDEV-6142 message" );
					expect( req.filecontent ).toInclude( "LDEV-6142 detail" );
					expect( req.filecontent ).toInclude( "LDEV-6142 extended info" );
					expect( req.filecontent ).notToInclude( heading( 3, "Message" ) );
				});

			});

		});
	}

	private function expectMarkdown( required string accept ) {
		var req = doRequest( arguments.accept );
		expect( req.status_code ).toBe( 500 );
		expect( req.headers[ "Content-Type" ] ).toBe( "text/markdown; charset=utf-8" );
		expect( req.filecontent ).toInclude( heading( 3, "Message" ) );
		expect( req.filecontent ).notToInclude( 'id="-lucee-err"' );
	}

	private function expectHTML( required string accept ) {
		var req = doRequest( arguments.accept );
		expect( req.status_code ).toBe( 500 );
		expect( req.headers[ "Content-Type" ] ).toInclude( "text/html" );
		expect( req.filecontent ).toInclude( 'id="-lucee-err"' );
		expect( req.filecontent ).notToInclude( heading( 3, "Message" ) );
	}

	private function doRequest( required string accept, string template="/throw.cfm" ) {
		return _InternalRequest(
			template: "#uri##arguments.template#",
			headers: { "Accept": arguments.accept },
			throwonerror: false
		);
	}

	private string function heading( required numeric level, required string text ) {
		return repeatString( chr( 35 ), arguments.level ) & " " & arguments.text;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & calledName;
	}

}
