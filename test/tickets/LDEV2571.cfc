component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2571", function() {
			it(title = "serialize", body = function( currentSpec ) {
				var sct = ["validate":false,"secure":"checkpoint"];
				var ser = serialize( sct );
				evaluate( ser);
				expect(ser)
				.toBe('["validate":false,"secure":"checkpoint"]');
			});
			
			it(title = "serializeJson", body = function( currentSpec ) {
				var sct = ["validate":false,"secure":"checkpoint"];
				var ser = serializeJson( sct );
				deserializeJson( ser);
				expect(ser)
				.toBe('{"validate":false,"secure":"checkpoint"}');
			});

		});
	}
}