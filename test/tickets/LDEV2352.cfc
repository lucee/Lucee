component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults, testBox ){
		describe( "Test suite for LDEV2352", function(){
			it( title = "test enum handled as simple values", body = function( currentSpec ){
				var SortOrder=createObject("java", "javax.swing.SortOrder");
				var sortOrders = SortOrder.values();


				var str="";
				for (var o in sortOrders) {
					str&=o&"-";
				    if (o == SortOrder.ASCENDING) str&="ASC;";
				    else if (o == SortOrder.DESCENDING) str&="DESC;";
				    else if (o == SortOrder.UNSORTED) str&="UNS;";
				}
				expect(str)
				.toBe("ASCENDING-ASC;DESCENDING-DESC;UNSORTED-UNS;");
			});
		});
	}

}