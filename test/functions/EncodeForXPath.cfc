component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForXPathMember", function() {
			it(title = "Checking with EncodeForXPathMember", body = function( currentSpec ) {
				enc='<script>'.EncodeForXPath();
				assertEquals('&lt;script&gt;',enc);
			});
		});	
	}
}