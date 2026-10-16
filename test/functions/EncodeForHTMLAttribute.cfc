component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForHTMLAttribute", function() {
			it(title = "Checking with EncodeForHTMLAttribute", body = function( currentSpec ) {
				var enc=EncodeForHTMLAttribute('<script>');
				assertEquals(0, find("<",enc));
			});
			it(title = "Checking with EncodeForHTMLAttributeMember", body = function( currentSpec ) {
				var enc='<script>'.EncodeForHTMLAttribute();
				assertEquals(0, find("<",enc));
			});
		});	
	}
}				