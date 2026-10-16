component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function beforeAll(){
		variables.uri = createURI( "LDEV6114" );
	}

	function run( testResults, testBox ){
		describe( "LDEV-6114 multi-byte unicode chars break cfhtmlhead insertion", function(){

			it( title="cfhtmlhead inserts correctly with İ (U+0130) in title", body=function( currentSpec ){
				local.result = _InternalRequest(
					template: "#uri#/htmlhead-multibyte-title.cfm"
				);
				local.content = trim( result.filecontent );
				expect( content ).toInclude( "<meta name='test' /></head>" );
				expect( content ).notToInclude( "<<meta" );
			});

			it( title="cfhtmlbody inserts correctly with İ (U+0130) in title", body=function( currentSpec ){
				local.result = _InternalRequest(
					template: "#uri#/htmlbody-multibyte-title.cfm"
				);
				local.content = trim( result.filecontent );
				expect( content ).toInclude( "<script src='test.js'></script></body>" );
				expect( content ).notToInclude( "<<script" );
			});

		});

		describe( "LDEV-6114 multi-byte unicode chars break string functions", function(){

			it( title="findNoCase returns correct position with İ (U+0130) before needle", body=function( currentSpec ){
				var str = "İİİ</head>";
				expect( findNoCase( "</head>", str ) ).toBe( 4 );
			});

			it( title="replaceNoCase works with İ (U+0130) in string", body=function( currentSpec ){
				var str = "İİİ</head>";
				expect( replaceNoCase( str, "</head>", "REPLACED" ) ).toBe( "İİİREPLACED" );
			});

		});
	}

	private string function createURI( string calledName ){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
