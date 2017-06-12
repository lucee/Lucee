component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1307", body=function() {
			it(title="checking dateTimeFormat member function", body = function( currentSpec ) {
				var result = now().dateTimeFormat("HH:NN:SS");
				expect(result).toBeTypeof('date');
			});
		});
	}
}