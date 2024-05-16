component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForHTML", function() {
			it(title = "Checking with EncodeForHTML", body = function( currentSpec ) {
				enc=EncodeForHTML('<script>');
				assertEquals('&lt;script&gt;',enc);
			});
			it(title = "Checking with EncodeForHTMLMember", body = function( currentSpec ) {
				enc='<script>'.EncodeForHTML();
				assertEquals('&lt;script&gt;',enc);
			});
		});	
	}
}