component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function testQueryToString(){
		loop times=5 {
			loop list="10,100,1000,10000" item="local.size" {
				systemOutput("", true);
				systemOutput("Query.toString() with #numberFormat(size)# rows", true);
				var a = queryNew( "id,name,created,updated" );
				loop times="#size#" {
					var r = queryAddRow( a );
					querySetCell(a, "id", repeatString(r,18), r)
				}
				var s = getTickCount();
				var aa = a.toString();
				systemOutput("string length: " & numberFormat(len(aa)) & " in #numberformat(getTickCount()-s)# ms ", true);
				systemOutput("", true);
			}
		}
	}

}

