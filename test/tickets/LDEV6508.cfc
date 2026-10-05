component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.preciseMath = getApplicationSettings().preciseMath;
	}

	function afterAll(){
		application action="update" preciseMath=variables.preciseMath;
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6508: number to string conversion must not serialise on a shared DecimalFormat", body=function() {

			afterEach( function(){
				application action="update" preciseMath=variables.preciseMath;
			});

			it( title="preciseMath=false: doubles are formatted with up to 12 decimal places, without trailing zeros", body=function( currentSpec ) {
				application action="update" preciseMath=false;
				expect( "" & ( 1 / 3 ) ).toBe( "0.333333333333" );
				expect( "" & ( 2 / 3 ) ).toBe( "0.666666666667" );
				expect( "" & ( -1 / 3 ) ).toBe( "-0.333333333333" );
				expect( toString( 1.5 ) ).toBe( "1.5" );
				expect( "#( 10 * 10 )#" ).toBe( "100" );
			});

			it( title="preciseMath=true: fractional BigDecimals are formatted with up to 16 decimal places, without trailing zeros", body=function( currentSpec ) {
				application action="update" preciseMath=true;
				expect( "" & ( 1 / 3 ) ).toBe( "0.3333333333333333" );
				expect( "" & ( 2 / 3 ) ).toBe( "0.6666666666666667" );
				expect( toString( 1.5 ) ).toBe( "1.5" );
				expect( "#( 1.50 + 0.50 )#" ).toBe( "2" );
			});

			it( title="preciseMath=true: whole number BigDecimals are returned as plain integers (no decimal point or exponent)", body=function( currentSpec ) {
				application action="update" preciseMath=true;
				expect( "#( 10 * 10 )#" ).toBe( "100" );
				expect( "#( -7 * 3 )#" ).toBe( "-21" );
				expect( "#( 123456789 * 1000 )#" ).toBe( "123456789000" );
			});

			it( title="concurrent threads converting the same doubles get identical, correct results", body=function( currentSpec ) {
				application action="update" preciseMath=false;
				var values = [];
				var expected = [];
				for ( var i = 1; i <= 200; i++ ) {
					arrayAppend( values, i / 7 );
					arrayAppend( expected, "" & ( i / 7 ) );
				}

				var threadNames = [];
				var prefix = "ldev6508_" & createUUID() & "_";
				for ( var t = 1; t <= 8; t++ ) {
					var threadName = prefix & t;
					arrayAppend( threadNames, threadName );
					thread name=threadName action="run" values=values expected=expected {
						thread.errors = [];
						for ( var loop = 1; loop <= 100; loop++ ) {
							for ( var j = 1; j <= arrayLen( attributes.values ); j++ ) {
								var str = "" & attributes.values[ j ];
								if ( compare( str, attributes.expected[ j ] ) != 0 ) {
									arrayAppend( thread.errors, str & " != " & attributes.expected[ j ] );
								}
							}
						}
					}
				}
				thread action="join" name=arrayToList( threadNames );

				for ( var name in threadNames ) {
					var result = cfthread[ name ];
					expect( result.status ).toBe( "COMPLETED", result.error ?: "" );
					expect( result.errors ).toBeEmpty();
				}
			});

		});
	}
}
