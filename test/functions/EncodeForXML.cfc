component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi,xml"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForXML", function() {
			it(title = "Checking with EncodeForXML", body = function( currentSpec ) {
				enc=EncodeForXML('<script>');
				assertEquals('&##x3c;script&##x3e;',enc);
			});

			it(title = "Checking with EncodeForXMLMember", body = function( currentSpec ) {
				enc='<script>'.EncodeForXML();
				assertEquals('&##x3c;script&##x3e;',enc);
			});
		});	
	}
}