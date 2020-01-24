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

			it(title = "Regression in struct implementation causing stack overflow", body = function( currentSpec ) {
				
			
				var struct1 = {children: [{}]}
				var struct2 = { parent: struct1 };
				struct1.children.append(struct2);
				var childIndex = struct2.parent.children.find(struct2);
			});


		});
	}
}