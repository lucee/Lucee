component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" skip=true{
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4023", function() {
			it( title="Checking arrayEach() with the array that has null values", body=function( currentSpec ) {
				var arr = [nullValue(), "second", nullValue(), "fourth"];
				var count = 0;
				var a = [];
				arr.each((e) => {
					count++;
					arrayAppend(a, isNull(arguments.e)?"":arguments.e);
				});
				expect(count).toBe(4);
				expect(arrayToList(a)).toBe(",second,,fourth");
			});
		});
	}
}