component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi"{
	function run( testResults , testBox ) {
		describe( "test case for ESAPIEncode", function() {
			it(title = "Checking with ESAPIEncode", body = function( currentSpec ) {
				enc=ESAPIEncode('html','<script>');
				assertEquals('&lt;script&gt;',enc);
			});
		});	
	}
}