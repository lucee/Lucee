component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		mylist="hoo,beep";
		describe( title="Test suite for listPrepend", body=function() {
			it( title='Checked with listPrepend()',body=function( currentSpec ) {
				assertEquals('"aaa,,John,aaa||||%%%%!||,,&&||||||*(*()$$$&&&&Ringo,AAA,AA|,,,|||hoo,beep"',serialize(listPrepend(mylist,"aaa,,John,aaa||||%%%%!||,,&&||||||*(*()$$$&&&&Ringo,AAA,AA|,,,||","|,&")));
				assertEquals('"aaa,,John,aaa||||%%%%!||,,&&||||||*(*()$$$&&&&Ringo,AAA,AA|,,,||&hoo,beep"',serialize(listPrepend(mylist,"aaa,,John,aaa||||%%%%!||,,&&||||||*(*()$$$&&&&Ringo,AAA,AA|,,,||","&")));
				assertEquals('",,waste,,,worth,,,hoo,beep"',serialize(listPrepend(mylist,",,waste,,,worth,,",",")));
				assertEquals('",,a,,,b,,,hoo,beep"',serialize(listPrepend(list=mylist,value=",,a,,,b,,",delimiter=",")));
				assertEquals('",,a,,,b,, hoo,beep"',serialize(listPrepend(list=mylist,value=",,a,,,b,,",delimiter=" ")));
			});
		});
		
	}
}