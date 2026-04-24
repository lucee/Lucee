component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "Test checkVersionGTE()", function() {

			describe( "major version comparisons", function() {
				it( "higher major returns true", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 5 ) ).toBeTrue( "6.2.1.25 >= 5" );
					expect( server.checkVersionGTE( "7.0.0.1", 6 ) ).toBeTrue( "7.0.0.1 >= 6" );
				});
				it( "equal major returns true", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 6 ) ).toBeTrue( "6.2.1.25 >= 6" );
				});
				it( "lower major returns false", function(){
					expect( server.checkVersionGTE( "5.4.7.3", 6 ) ).toBeFalse( "5.4.7.3 >= 6" );
					expect( server.checkVersionGTE( "5.4.7.3", 7 ) ).toBeFalse( "5.4.7.3 >= 7" );
				});
			});

			describe( "minor version comparisons", function() {
				it( "higher minor returns true", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 6, 1 ) ).toBeTrue( "6.2.1.25 >= 6.1" );
					expect( server.checkVersionGTE( "5.4.7.3", 5, 4 ) ).toBeTrue( "5.4.7.3 >= 5.4" );
				});
				it( "lower minor returns false", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 6, 3 ) ).toBeFalse( "6.2.1.25 >= 6.3" );
					expect( server.checkVersionGTE( "5.4.7.3", 6, 1 ) ).toBeFalse( "5.4.7.3 >= 6.1" );
				});
			});

			describe( "patch version comparisons", function() {
				it( "higher patch returns true", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 6, 2, 0 ) ).toBeTrue( "6.2.1.25 >= 6.2.0" );
					expect( server.checkVersionGTE( "5.4.7.3", 5, 4, 6 ) ).toBeTrue( "5.4.7.3 >= 5.4.6" );
				});
				it( "equal patch returns true", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 6, 2, 1 ) ).toBeTrue( "6.2.1.25 >= 6.2.1" );
					expect( server.checkVersionGTE( "5.4.7.3", 5, 4, 7 ) ).toBeTrue( "5.4.7.3 >= 5.4.7" );
				});
				it( "lower patch returns false", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 6, 2, 2 ) ).toBeFalse( "6.2.1.25 >= 6.2.2" );
				});
			});

			describe( "build version comparisons", function() {
				it( "higher build returns true", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 6, 2, 1, 24 ) ).toBeTrue( "6.2.1.25 >= 6.2.1.24" );
					expect( server.checkVersionGTE( "5.4.7.3", 5, 4, 7, 2 ) ).toBeTrue( "5.4.7.3 >= 5.4.7.2" );
				});
				it( "exact match returns true", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 6, 2, 1, 25 ) ).toBeTrue( "6.2.1.25 >= 6.2.1.25" );
					expect( server.checkVersionGTE( "5.4.7.3", 5, 4, 7, 3 ) ).toBeTrue( "5.4.7.3 >= 5.4.7.3" );
				});
				it( "lower build returns false", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 6, 2, 1, 26 ) ).toBeFalse( "6.2.1.25 >= 6.2.1.26" );
					expect( server.checkVersionGTE( "5.4.7.3", 5, 4, 7, 4 ) ).toBeFalse( "5.4.7.3 >= 5.4.7.4" );
				});
			});

			describe( "cross-component comparisons", function() {
				it( "higher major trumps lower minor/patch/build", function(){
					expect( server.checkVersionGTE( "6.2.1.25", 5, 4, 7, 3 ) ).toBeTrue( "6.2.1.25 >= 5.4.7.3" );
				});
				it( "higher patch trumps lower build", function(){
					expect( server.checkVersionGTE( "7.0.4.17", 7, 0, 0, 115 ) ).toBeTrue( "7.0.4.17 >= 7.0.0.115" );
				});
				it( "lower major fails regardless of other components", function(){
					expect( server.checkVersionGTE( "5.4.7.3", 6, 2, 1, 25 ) ).toBeFalse( "5.4.7.3 >= 6.2.1.25" );
				});
			});

		});
	}
}