component extends="org.lucee.cfml.test.LuceeTestCase" labels="guard" {
	function run( testResults , testBox ) {
		describe( "test case for EncodeForXPathMember", function() {
			it(title = "Checking with EncodeForXPathMember", body = function( currentSpec ) {
				var enc='<script>'.EncodeForXPath();
				assertEquals('&lt;script&gt;',enc);
			});
		});	
	}
}