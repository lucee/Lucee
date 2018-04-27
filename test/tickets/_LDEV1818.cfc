component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1818", body=function() {
			it(title = "Checking Interface Methods while doesn't use semicolon", body = function( currentSpec ) {
				try{
					var instance = new 'LDEV1818.Sample'();
					local.result = instance.name("lucee");
				} catch( any e ){
					local.result = e.Message ;
				}
				expect(local.result).toBe('lucee');
			});
		});
	}
} 