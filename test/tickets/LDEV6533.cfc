component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6533 final in component pseudo-constructor", function() {

			it( title="final numeric in variables scope returns the value not DataMember", body=function() {
				var info = new LDEV6533.Leak().info();
				expect( info.isNumeric ).toBeTrue();
				expect( listLast( info.numClass, "." ) ).notToBe( "DataMember" );
				expect( info.plainClass ).toBe( info.numClass );
				expect( info.num ).toBe( 5 );
			});

			it( title="final closure in variables scope is callable", body=function() {
				var info = new LDEV6533.Leak().info();
				expect( info.call ).toBe( "fn" );
			});

			it( title="overwriting a final variables member throws", body=function() {
				expect( function() {
					new LDEV6533.Overwrite().touch();
				} ).toThrow();
			});
		});
	}
}
