component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1834", function() {
			it( title='Checking Default functions for interface', body=function( currentSpec ) {
				var myobj=new LDEV1835.Comp();
				assertEquals("Hello world", myobj.returnsany("hello world"));
			});
		});
	}
}