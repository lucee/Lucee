component extends="org.lucee.cfml.test.LuceeTestCase" labels="collection" {
	function run( testResults, textbox ) {
		describe("testcase for collectionReduce()", function() {
			variables.thresholds = [1, 3, 4, 5];
			it(title="checking collectionReduce() function", body=function( currentSpec ) {
				var score = collectionReduce(thresholds, function(a, b) {
					return a + b^2;
				}, 0);
				assertEquals('51', score);
			});
			
			it(title="checking collection.reduce() function", body=function( currentSpec ) {
				var score = thresholds.reduce(function(a, b) {
					return a + b^2;
				}, 0);
				assertEquals('51', score);
			});
		});
	}
}