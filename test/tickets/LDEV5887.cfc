component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.preciseMath = getApplicationSettings().preciseMath;
	}

	function afterAll() {
		application action="update" preciseMath=variables.preciseMath;
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-5887 cached preciseMath flag on the PageContext", body=function() {

			afterEach( function() {
				application action="update" preciseMath=variables.preciseMath;
			});

			it( title="toggling preciseMath within a request takes effect immediately", body=function( currentSpec ) {
				application action="update" preciseMath=true;
				expect( getMetadata( 1 + 0.1 ).getName() ).toBe( "java.math.BigDecimal" );
				expect( toString( 0.1 + 0.2 ) ).toBe( "0.3" );
				application action="update" preciseMath=false;
				expect( getMetadata( 1 + 0.1 ).getName() ).toBe( "java.lang.Double" );
				application action="update" preciseMath=true;
				expect( getMetadata( abs( -1.5 ) ).getName() ).toBe( "java.math.BigDecimal" );
			});

			it( title="child threads see the preciseMath setting of the application", body=function( currentSpec ) {
				application action="update" preciseMath=false;
				var t = "ldev5887_" & createUUID();
				thread name=t {
					thread.cls = getMetadata( abs( -1.5 ) ).getName();
				}
				threadJoin( t );
				expect( cfthread[ t ].cls ).toBe( "java.lang.Double" );
			});
		});
	}
}
