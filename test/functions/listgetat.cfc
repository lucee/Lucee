component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		mylist = "aaa,,John,aaa||||%%%%!||,,&&||||||*(*()$$$&&&&Ringo,AAA,AA|,,,||";

		describe( title ="Test suite for listGetAt", body = function() {
			it( title = 'Test case for listGetAt', body = function( currentSpec ) {
				assertEquals('"aaa"',serialize(listGetAt(mylist,1,",",false)));
				assertEquals('"aaa"',serialize(listGetAt(mylist,1,",",true)));
				assertEquals('"John"',serialize(listGetAt(mylist,3,",",true)));
				assertEquals('",,John,"',serialize(listGetAt(mylist,1,false)));
				assertEquals('"aaa,,John,aaa||||%%%%!||,,&&||||||*(*()$$$&&&&Ringo,AAA,AA|,,,||"',serialize(listGetAt(mylist,1,true)));
				assertEquals('"aaa,,John,aaa"',serialize(listGetAt(mylist,1,"|",false)));
				assertEquals('"aaa,,John,aaa"',serialize(listGetAt(mylist,1,"|",true)));
				assertEquals('""',serialize(listGetAt(mylist,4,"|",true)));
				assertEquals('"%%%%!"',serialize(listGetAt(mylist,5,"|",true)));
				assertEquals('"aaa,,John,aaa||||%%%%!||,,"',serialize(listGetAt(mylist,1,"&",false)));
				assertEquals('"||||||*(*()$$$"',serialize(listGetAt(mylist,3,"&",true)));
				assertEquals('"aaa,,John,aaa||||%%%%!||,,"',serialize(listGetAt(mylist,1,"&",true)));
			});
			it( title = 'Test case for list.listGetAt member function', body = function( currentSpec ) {
				assertEquals('"aaa"',serialize(mylist.listGetAt(1,",",false)));
				assertEquals('"aaa"',serialize(mylist.listGetAt(1,",",true)));
				assertEquals('"John"',serialize(mylist.listGetAt(3,",",true)));
				assertEquals('",,John,"',serialize(mylist.listGetAt(1,false)));
				assertEquals('"aaa,,John,aaa||||%%%%!||,,&&||||||*(*()$$$&&&&Ringo,AAA,AA|,,,||"',serialize(mylist.listGetAt(1,true)));
				assertEquals('"aaa,,John,aaa"',serialize(mylist.listGetAt(1,"|",false)));
				assertEquals('"aaa,,John,aaa"',serialize(mylist.listGetAt(1,"|",true)));
				assertEquals('""',serialize(mylist.listGetAt(4,"|",true)));
				assertEquals('"%%%%!"',serialize(mylist.listGetAt(5,"|",true)));
				assertEquals('"aaa,,John,aaa||||%%%%!||,,"',serialize(mylist.listGetAt(1,"&",false)));
				assertEquals('"||||||*(*()$$$"',serialize(mylist.listGetAt(3,"&",true)));
				assertEquals('"aaa,,John,aaa||||%%%%!||,,"',serialize(mylist.listGetAt(1,"&",true)));
			});
		});
		
	}
}