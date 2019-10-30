component extends = "org.lucee.cfml.test.LuceeTestCase"	{
	function run( testResults , testBox ) {
		string = "1'st think i am going to loss weight upto 10 to 15 kg's";
		describe( title = "Test suite for rematch in function", body = function() {
			it( title = 'Test case for rematch in function',body = function( currentSpec ) {
				assertEquals([1,10,15],rematch("[0-9]+",string));
				assertEquals("TRUE",isarray(rematch("[0-9]+",string)));
				assertEquals("3",arraylen(rematch("[0-9]+",string)));
			});
			it( title = 'Test case for rematch in member-function',body = function( currentSpec ) {
				assertEquals([1,10,15],string.rematch("[0-9]+"));
				assertEquals("TRUE",isarray(string.rematch("[0-9]+")));
				assertEquals("3",arraylen(rematch("[0-9]+",string)));
			});
		})
	}
}