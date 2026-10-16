component extends="org.lucee.cfml.test.LuceeTestCase" labels="component" {

	function run( testResults, testBox ) {
		describe( "LDEV-6571 short sub component name as return/argument type", function() {

			it( "init() with returntype of the short sub component name works", function() {
				var c = new LDEV6571.Comp();
				expect( isInstanceOf( c.getSub(), "Sub" ) ).toBeTrue();
			});

			it( "short sub component name works as return type", function() {
				var c = new LDEV6571.Comp();
				expect( isObject( c.returnShort() ) ).toBeTrue();
			});

			it( "short sub component name works as argument type", function() {
				var c = new LDEV6571.Comp();
				expect( c.acceptShort( c.getSub() ) ).toBeTrue();
			});

			it( "parent component is not an instance of the sub component name", function() {
				var c = new LDEV6571.Comp();
				expect( isInstanceOf( c, "Sub" ) ).toBeFalse();
				expect( function() { c.returnWrong(); } ).toThrow();
			});

			it( "cast error names the sub component, not the parent", function() {
				var c = new LDEV6571.Comp();
				var msg = "";
				try {
					c.acceptParent( c.getSub() );
				}
				catch ( e ) {
					msg = e.message & " " & e.detail;
				}
				expect( msg ).toInclude( "Comp$Sub" );
			});

		});
	}
}
