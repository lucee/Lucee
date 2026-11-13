component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-3913", function() {

			it( title="toString() of an xml document with the root element html and a child element head does not add a META tag", body=function( currentSpec ) {
				var xml = xmlParse( "<html><head/></html>" );
				var str = toString( xml );
				expect( str ).notToInclude( "META" );
				expect( str ).notToInclude( "http-equiv" );
				expect( str ).toInclude( "<head/>" );
			});

			it( title="the string of such an xml document can be parsed again and gives the same structure", body=function( currentSpec ) {
				var xml = xmlParse( "<html><head><title>Lucee</title></head><body><p>hello</p></body></html>" );
				var again = xmlParse( toString( xml ) );
				expect( again.xmlRoot.xmlName ).toBe( "html" );
				expect( arrayLen( again.xmlRoot.head.xmlChildren ) ).toBe( 1 );
				expect( again.xmlRoot.head.title.xmlText ).toBe( "Lucee" );
				expect( again.xmlRoot.body.p.xmlText ).toBe( "hello" );
			});

			it( title="the root element html in upper case", body=function( currentSpec ) {
				var str = toString( xmlParse( "<HTML><HEAD/></HTML>" ) );
				expect( str ).notToInclude( "META" );
			});

			it( title="an xml document with another root element is not changed", body=function( currentSpec ) {
				var str = toString( xmlParse( "<root><head/></root>" ) );
				expect( str ).toInclude( "<root><head/></root>" );
			});

			it( title="toString() of an html root element node", body=function( currentSpec ) {
				var xml = xmlParse( "<html><head/></html>" );
				expect( toString( xml.xmlRoot ) ).notToInclude( "META" );
			});
		});
	}
}
