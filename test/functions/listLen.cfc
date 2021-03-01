component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ){

		describe( title = "Test case for listLen()", body = function() {
			it( title = "Checking with listlen()", body = function( currentSpec ) {
				assertEquals("0",listLen(''));
				assertEquals("1",listLen('aaa'));
				assertEquals("1",listLen(',,aaa,,'));
				assertEquals("3",listLen('aaa,bbb,ccc'));
				assertEquals("3",listLen('aaa,bbb,ccc,,'));
				assertEquals("3",listLen('aaa,,,,,bbb,ccc,,'));
				assertEquals("5",listLen('aaa,b.bb,c,.cc',',.'));
				assertEquals("3",listLen(',,a,,b,,c,,',',',false));
				assertEquals("9",',,a,,b,,c,,'.listLen(',',true));
				assertEquals("3",',,a,,b,,c,,'.listLen(',.',false));
				assertEquals("9",',,a,,b,,c,,'.listLen(',.',true));
			});
		});
	}
}