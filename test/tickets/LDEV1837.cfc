component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1837", body=function() {
			it(title = "checking toString", body = function( currentSpec ) {
				var str=server.os.macAddress&"";
			});
			it(title = "checking serialize", body = function( currentSpec ) {
				serialize(server.os.macAddress);
			});
			it(title = "checking serialzeJson", body = function( currentSpec ) {
				serializeJson(server.os.macAddress);
			});
		});
	}
} 