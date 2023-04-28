component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4460", function() {
			it( title="checking imageAddBorder() with negative value of thickness argument", body=function( currentSpec ) {
				expect( function(){
					var img = imageNew("",100,80,"rgb","B33771");
					imageAddBorder(img,-23,"blue");
				}).toThrow();
			});
		});
	}
}