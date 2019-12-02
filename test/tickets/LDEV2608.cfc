component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2608", function() {
			it(title = "Regression in struct implementation causing stack overflow", body = function( currentSpec ) {
				struct1 = {};
				struct2 = {};
				struct1.struct2 = struct2;
				struct2.struct1 = struct1;

				expect(struct1.equals(struct1)).toBe('YES');
			});

			it(title = "Regression in struct implementation causing stack overflow", body = function( currentSpec ) {
				struct1 = {};
				struct2 = {};
				struct1.struct2 = struct2;
				struct2.struct1 = struct1;

				expect(struct1.equals(struct2)).toBe('NO');
			});
		});
	}
}