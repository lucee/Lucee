component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe(title="Test cases for LDEV-1620", body=function() {
			it(title = "Checking .ico images", body = function( currentSpec ) {
				var hasError = false;
				try{
					myImage=ImageRead("LDEV1620\fonts.ico"); 
				} catch ( any e ){
					hasError = true;
				}
				expect(hasError).toBe(false);
			});
		});
	}
}
