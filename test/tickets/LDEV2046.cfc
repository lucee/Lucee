component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2046", function() {
			it(title = "structGet() incompatibility with ACF", body = function( currentSpec ) {
				var outerStruct = {
							nestStructOne: {
								nestStructTwo: {
									key1: true,
									key2: true,
									key3: true
								}
							}
						};
				var result = StructGet("outerStruct.nestStructOne.nestStructTwo.key1");
				expect(result).toBe('true');
			});
		});
	}
}