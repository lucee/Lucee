component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4459", function() {
			it( title="checking Imageshear() with shear argument", body=function( currentSpec ) {
				expect( function(){
					var img = imageNew("",100,80,"rgb","B33771");
					img.Shear(37);
				}).notToThrow();
			});
		});
	}
}