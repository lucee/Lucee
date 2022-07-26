component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForURL", function() {
			it(title = "Checking with EncodeForURL", body = function( currentSpec ) {
				enc=EncodeForURL('<script>');
				assertEquals('%3Cscript%3E',enc);
			});

			it(title = "Checking with EncodeForURLMember", body = function( currentSpec ) {
				enc='<script>'.EncodeForURL();
				assertEquals('%3Cscript%3E',enc);
			});
		});	
	}
}