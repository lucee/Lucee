component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for decodeFromURL", function() {
			it(title = "Checking with decodeFromURL", body = function( currentSpec ) {
				enc=EncodeForURL('<script>');
				assertEquals('%3Cscript%3E',enc);
				assertEquals('<script>',decodeFromURL(enc));
			});
		});	
	}
}