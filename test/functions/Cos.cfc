component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for Cos()", body=function() {
			it(title="checking Cos() function", body = function( currentSpec ) {
				assertEquals("-0.989992496600","#left(tostring(cos(3)),15)#");
			});
		});
	}
}