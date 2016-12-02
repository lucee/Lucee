component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-584", function() {
			it(title="checking listRemoveDuplicates function, having simple list with duplicate values", body = function( currentSpec ) {
				var list = '1,7,7,10,6,7,8';
				var result = listRemoveDuplicates(list);
				expect(result).toBe('1,7,10,6,8');
			});

			it(title="checking listRemoveDuplicates function, having duplicate value at last", body = function( currentSpec ) {
				var list = '1,7,7,10,6,7';
				var result = listRemoveDuplicates(list);
				expect(result).toBe('1,7,10,6');
			});

			it(title="checking listRemoveDuplicates function, having empty value at last", body = function( currentSpec ) {
				var list = '1,7,7,10,6, ';
				var result = listRemoveDuplicates(list);
				expect(result).toBe('1,7,10,6, ');
			});
		});
	}
}
