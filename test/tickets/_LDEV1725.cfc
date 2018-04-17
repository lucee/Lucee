component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test suite for LDEV-1725", body=function(){
			it(title="Checking ListLast with attribute count", body=function(){
				var list = "A,B,C,D,E,F,G";
				var list2 = "1%2%3%4%5%6%7";
				var list3 = "1%2,3%4,5%6,7";
				assertEquals("D,E,F,G", listLast(list, ",", false, 4));
				assertEquals("A,B,C,D,E,F,G", listLast(list, ",", false, 10));
				assertEquals("G", listLast(list, ",", false, 1));
				assertEquals("5%6%7", listLast(list2, "%", false, 3));
				assertEquals("4%5%6%7", listLast(list2, "%", false, 4));
				assertEquals("7", listLast(list2, "%", false, 1));
				assertEquals("1%2%3%4%5%6%7", listLast(list2, "%", false, 10));
				assertEquals("4,5%6,7", listLast(list3, "%,", false, 4));
				assertEquals("1%2,3%4,5%6,7", listLast(list3, "%,", false, 10));
				assertEquals("7", listLast(list3, "%,", false, 1));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}