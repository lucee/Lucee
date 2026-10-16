component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.preciseMath = getApplicationSettings().preciseMath;
	}

	function afterAll() {
		application action="update" preciseMath=variables.preciseMath;
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for pi()", body=function() {

			afterEach( function() {
				application action="update" preciseMath=variables.preciseMath;
			});

			it( title="pi() with preciseMath=true returns correct digits", body=function( currentSpec ) {
				application action="update" preciseMath=true;
				expect( toString( pi() ) ).toBe( "3.1415926535897932" );
				// full precision of the BigDecimal constant
				expect( left( pi().toPlainString(), 52 ) ).toBe( "3.14159265358979323846264338327950288419716939937510" );
			});

			it( title="pi() with preciseMath=false matches java Math.PI", body=function( currentSpec ) {
				application action="update" preciseMath=false;
				expect( pi() ).toBe( createObject( "java", "java.lang.Math" ).PI );
			});
		});
	}
}
