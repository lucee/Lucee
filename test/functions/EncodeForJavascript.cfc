component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForJavascript", function() {
			it(title = "Checking with EncodeForJavascript", body = function( currentSpec ) {
				enc=EncodeForJavascript('<script>');
				assertEquals('\x3Cscript\x3E',enc);
			});
			it(title = "Checking with EncodeForJavascriptMember", body = function( currentSpec ) {
				enc='<script>'.EncodeForJavascript();
				assertEquals('\x3Cscript\x3E',enc);
			});
		});	
	}
}