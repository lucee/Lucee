component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForDN", function() {
			it(title = "Checking EncodeForDN() function", body = function( currentSpec ) {
				enc=EncodeForDN('<script>');
				assertEquals('\<script\>',enc);
			});
			it(title = "Checking testEncodeForDNMember", body = function( currentSpec ) {
				enc='<script>'.EncodeForDN();
				assertEquals('\<script\>',enc);
			});	
		});
	}
}