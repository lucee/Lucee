component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "Testcase for LDEV-4828", function(){
			it( title="dump crashes with 31 columns", body=function( currentSpec ) {
				var q = queryNew("")
				var numberOfColumns = 31 // 30 works OK, 31 throws error: The value [2147483648] is outside the range that can be represented as an int.
				for(var i=1; i<=numberOfColumns; i++) {
					queryAddColumn(q, "c#i#", ["x"])
				}
				dump(q)
			});
		});
	}

}
