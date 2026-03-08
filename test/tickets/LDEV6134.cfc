component extends="org.lucee.cfml.test.LuceeTestCase" labels="bytecode" {

	function run( testResults, testBox ) {
		describe( "LDEV-6134 - MethodTooLargeException in <init> constructor for components with many static strings", function() {
			it( title="component with  compiles and instantiates without MethodTooLargeException", body=function( currentSpec ) {
				// this will throw MethodTooLargeException if the <init> constructor is not split
				var obj = new LDEV6134.CfcWithExcessStaticObjectKeys( parent={} );
				expect( obj ).notToBeNull();
				expect( obj.testWord( "aaa" ) ).toBe( "aaa" );
			});
		});
	}

}
