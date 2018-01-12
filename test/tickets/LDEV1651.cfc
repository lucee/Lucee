component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test Case for LDEV-1651", function() {
			it( title='Checking directoryExists() is empty', body=function( currentSpec ) {
				expect(directoryExists("")).toBe('false');
			});
		});
	}
}
