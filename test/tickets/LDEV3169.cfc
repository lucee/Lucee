component extends="org.lucee.cfml.test.LuceeTestCase" labels="closure" {

	function run( testResults, testBox ) {
		describe( "LDEV-3169 some/every closure without a return value", function() {

			var check = function( fn ) {
				try {
					fn();
				}
				catch ( any e ) {
					return e.message;
				}
				return "no exception";
			};

			it( "structSome() error names the missing boolean return", function() {
				var msg = check( function() {
					structSome( { a: 1 }, function( k, v ) { var x = 1; } );
				});
				expect( msg ).notToInclude( "Can't cast String []" );
				expect( msg ).toInclude( "boolean" );
			});

			it( "structEvery() error names the missing boolean return", function() {
				var msg = check( function() {
					structEvery( { a: 1 }, function( k, v ) { var x = 1; } );
				});
				expect( msg ).notToInclude( "Can't cast String []" );
				expect( msg ).toInclude( "boolean" );
			});

			it( "arraySome() error names the missing boolean return", function() {
				var msg = check( function() {
					arraySome( [ 1 ], function( v ) { var x = 1; } );
				});
				expect( msg ).notToInclude( "Can't cast String []" );
				expect( msg ).toInclude( "boolean" );
			});

			it( "arrayEvery() error names the missing boolean return", function() {
				var msg = check( function() {
					arrayEvery( [ 1 ], function( v ) { var x = 1; } );
				});
				expect( msg ).notToInclude( "Can't cast String []" );
				expect( msg ).toInclude( "boolean" );
			});

			it( "returning a boolean still works", function() {
				expect( structSome( { a: 1 }, function( k, v ) { return v == 1; } ) ).toBeTrue();
				expect( arrayEvery( [ 1, 2 ], function( v ) { return v > 0; } ) ).toBeTrue();
			});
		});
	}
}
