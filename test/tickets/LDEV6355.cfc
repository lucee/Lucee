component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV6355" {

	function run( testResults, testBox ) {
		describe( "LDEV-6355 ConstructorInstance speculative constructor probes", function() {

			it( "a class without a matching constructor is not initialised by a probe", function() {
				var int = createObject( "java", "java.lang.Integer" );
				loop times=3 {
					expect( isStruct( int ) ).toBeFalse();
					expect( isArray( int ) ).toBeFalse();
					expect( isBoolean( int ) ).toBeFalse();
				}
				expect( int.valueOf( 5 ) ).toBe( 5 );
				expect( int.init( 7 ).intValue() ).toBe( 7 );
			});

			it( "calling an instance method of a class without a matching constructor fails with a clear message", function() {
				var int = createObject( "java", "java.lang.Integer" );
				expect( function() {
					int.intValue();
				}).toThrow( regex = "java\.lang\.Integer" );
			});
		});
	}
}
