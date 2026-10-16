component extends="org.lucee.cfml.test.LuceeTestCase" labels="bytecode" {

	function run( testResults, testBox ) {
		describe( "LDEV-6126 - MethodTooLargeException in <init> constructor for components with many functions", function() {
			it( title="component with 130+ functions compiles and instantiates without MethodTooLargeException", body=function( currentSpec ) {
				// this will throw MethodTooLargeException if the <init> constructor is not split
				var obj = new LDEV6126.CfcWith130Methods( parent={} );
				expect( obj ).notToBeNull();
				expect( obj.test1( "a", "b", "c", "d" ) ).toBe( "abcd" );
				expect( obj.test130( "a", "b", "c", "d" ) ).toBe( "abcd" );
			});
		});
	}

}
