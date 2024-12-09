component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
	};

	function afterAll(){
		application action="update" precisemath=variables.preciseMath;
	};

	function run( testResults, testBox ){
		describe( "Test ParseNumber", function(){

			it( "Test with smaller number", function(){
				var n = "2305843.44";
				application action="update" preciseMath=false;
				expect( parseNumber( n ) ).toBe( 2305843.44 );
				application action="update" preciseMath=true;
				expect( parseNumber( n ) ).toBe( 2305843.44 );
			});

			it( "Test with Large number", function(){
				var n = "2305843009213693951.77";
				application action="update" preciseMath=false;
				expect( parseNumber( n ) ).toBe( 2305843009213693696 );
				application action="update" preciseMath=true;
				expect( parseNumber( n ) ).toBe( 2305843009213693951 );  // returns 2305843009213693951.77
			});

			it( "Test Bin", function(){
				application action="update" preciseMath=false;
				var n = "1000";
				var radix = "bin";
				expect( parseNumber( n, radix ) ).toBe( 8 );
				application action="update" preciseMath=true;
				expect( parseNumber( n, radix ) ).toBe( 8 );
			});

			it( "Test Oct", function(){
				application action="update" preciseMath=false;
				var n = "1000";
				var radix = "oct";
				expect( parseNumber( n, radix ) ).toBe( 512 );
				application action="update" preciseMath=true;
				expect( parseNumber( n, radix ) ).toBe( 512 );
			});

			it( "Test Hex", function(){
				application action="update" preciseMath=false;
				var n = "1000";
				var radix = "hex";
				expect( parseNumber( n, radix ) ).toBe( 4096 );
				application action="update" preciseMath=true;
				expect( parseNumber( n, radix ) ).toBe( 4096 );
			});

		} );
	}

}
