component extends="org.lucee.cfml.test.LuceeTestCase" labels="esapi,xml"{
	function run( testResults , testBox ) {
		describe( "test case for EncodeForXML", function() {
			it(title = "Checking with EncodeForXML", body = function( currentSpec ) {
				var enc=EncodeForXML('<script>');
				assertEquals(true, enc=="&##x3c;script&##x3e;" || enc=="&lt;script&gt;");
			});

			it(title = "Checking with EncodeForXMLMember", body = function( currentSpec ) {
				var enc='<script>'.EncodeForXML();
				assertEquals(true, enc=="&##x3c;script&##x3e;" || enc=="&lt;script&gt;");
			});
		});	
	}
}