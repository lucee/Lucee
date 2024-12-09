component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
	};

	function afterAll(){
		application action="update" precisemath=variables.preciseMath;
	};

	function run( testResults, testBox ){
		describe( "Test LSParseNumber", function(){

			it( "Test with Large number", function(){
				var n = "2.305.843.009,01";
				var locale = "german (standard)";
				application action="update" preciseMath=false;
				expect( LSParseNumber( n, locale ) ).toBe( 2305843009 );
				application action="update" preciseMath=true;
				expect( LSparseNumber( n, locale ) ).toBe( 2305843009 ); // but it returns 2305843009.01 ?
			});

			it( "Test with Large number", function(){
				var n = "2.305.843.009.213.693.951,01";
				var locale = "german (standard)";
				application action="update" preciseMath=false;
				expect( LSParseNumber( n, locale ) ).toBe( 2305843009213693696 );
				application action="update" preciseMath=true;
				expect( LSparseNumber( n, locale ) ).toBe( 2305843009213693952 ); // LSParseNumber doesn't support big numbers
			});

		} );
	}

}
