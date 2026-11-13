component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {

	function run( testResults, testBox ) {
		describe( "LDEV-2154 xmlTransform() resolves relative xsl:include / xsl:import against the xsl file", function() {

			it( title="xsl:include with a relative href resolves against the folder of the xsl file", body=function( currentSpec ) {
				var xsl = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV2154/main.xsl";
				var result = xmlTransform( "<root/>", xsl );
				expect( trim( result ) ).toBe( "included" );
			});

			it( title="xsl:import with a relative href resolves against the folder of the xsl file", body=function( currentSpec ) {
				var xsl = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV2154/imported.xsl";
				var result = xmlTransform( "<root/>", xsl );
				expect( trim( result ) ).toBe( "included" );
			});

		});
	}

}
