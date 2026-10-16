component extends="org.lucee.cfml.test.LuceeTestCase" labels="guard" {
	function run( testResults , testBox ) {
		describe( "test case for EncodeForDN", function() {
			it(title = "Checking EncodeForDN() function", body = function( currentSpec ) {
				var enc=EncodeForDN('<script>');
				assertEquals('\<script\>',enc);
			});
			it(title = "Checking testEncodeForDNMember", body = function( currentSpec ) {
				var enc='<script>'.EncodeForDN();
				assertEquals('\<script\>',enc);
			});	
		});
	}
}