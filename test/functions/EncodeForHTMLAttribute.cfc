component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForHTMLAttribute", function() {
			it(title = "Checking with EncodeForHTMLAttribute", body = function( currentSpec ) {
				enc=EncodeForHTMLAttribute('<script>');
				assertEquals('&lt;script&gt;',enc);
			});
			it(title = "Checking with EncodeForHTMLAttributeMember", body = function( currentSpec ) {
				enc='<script>'.EncodeForHTMLAttribute();
				assertEquals('&lt;script&gt;',enc);
			});
		});	
	}
}				