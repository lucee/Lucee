component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ListToArray()", body=function() {
			it(title="checking ListToArray() function", body = function( currentSpec ) {
				<!--- begin old test code --->
				assertEquals("0", "#arrayLen(ListToArray(''))#");
				assertEquals("3", "#arrayLen(ListToArray('aaa,bbb,ccc'))#");
				assertEquals("1", "#arrayLen(ListToArray(',,xx,,'))#");
				assertEquals("3", "#arrayLen(ListToArray('xx,xx,xx'))#");
				assertEquals("3", "#arrayLen(ListToArray(',,xx,xx,xx'))#");
				assertEquals("3", "#ListToArray(',,xx,,,xx,xx').size()#");
				assertEquals("aa,a,bbb,c,cc", "#arrayToList(ListToArray('aayaxybbbxycxccx',"xy"))#");
				assertEquals("aaaUaaa", "#arrayToList(ListToArray('aaaUaaa',"u"))#");
				assertEquals("aaa,aaa", "#arrayToList(ListToArray('aaaUaaa',"U"))#");

				assertEquals("sasa,asaSa", "#arrayToList(ListToArray(',,sasa,,,asaSa,,',","))#");
				assertEquals(",,sasa,,,asaSa,,", "#arrayToList(ListToArray(',,sasa,,,asaSa,,',",",true))#");
				assertEquals("sasa,asaSa", "#arrayToList(ListToArray(',,sasa,,,asaSa,,',",",false))#");

				assertEquals("a*b*c", "#arrayToList(ListToArray('a:-b:-c',':-',false,false),'*')#");
				assertEquals("a*b*c", "#arrayToList(ListToArray('a-b:c',':-',false,false),'*')#");
				assertEquals("a*b*c", "#arrayToList(ListToArray('a:-b:-c',':-',false,true),'*')#");
				assertEquals("a-b:c", "#arrayToList(ListToArray('a-b:c',':-',false,true),'*')#");

				assertEquals("a*b*c", "#arrayToList(ListToArray(':-:-a:-b:-:-c:-:-',':-',false,true),'*')#");
				assertEquals("**a*b**c**", "#arrayToList(ListToArray(':-:-a:-b:-:-c:-:-',':-',true,true),'*')#");

				assertEquals("**a*b****", "#arrayToList(ListToArray(':-x:-xa:-xb:-x:-x:-x:-x',':-x',true,true),'*')#");

				assertEquals("0", "#arrayLen(ListToArray('',',',false))#");
				assertEquals("1", "#arrayLen(ListToArray('',',',true))#");
			});
		});
	}
}