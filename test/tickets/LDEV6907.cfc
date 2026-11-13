component extends="org.lucee.cfml.test.LuceeTestCase" labels="log" {

	function run( testResults, testBox ) {
		describe( "LDEV-6907 cflog with attributeCollection inside a loop, if or try", function() {

			var args = { text: "LDEV-6907", type: "information", log: "application" };

			it( "cflog with attributeCollection inside a for loop", function() {
				var count = 0;
				for ( var i = 1; i <= 3; i++ ) {
					cflog( attributeCollection = args );
					count++;
				}
				expect( count ).toBe( 3 );
			});

			it( "cflog with attributeCollection inside an if", function() {
				var logged = false;
				if ( true ) {
					cflog( attributeCollection = args );
					logged = true;
				}
				expect( logged ).toBeTrue();
			});

			it( "cflog with attributeCollection inside a try", function() {
				var logged = false;
				try {
					cflog( attributeCollection = args );
					logged = true;
				}
				catch ( any e ) {}
				expect( logged ).toBeTrue();
			});

			it( "cflog with attributes inside a for loop", function() {
				var count = 0;
				for ( var i = 1; i <= 3; i++ ) {
					cflog( text = "LDEV-6907", type = "information", log = "application" );
					count++;
				}
				expect( count ).toBe( 3 );
			});

		});
	}
}
