<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( "test", function() {
			it( "works", function() {
				expect( true ).toBe( true );
			});
		});
	}
}
</cfscript>
