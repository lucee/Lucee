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
                assertEquals("1",listLen('',',',true));
				assertEquals("5",listLen(',,aaa,,',',',true));
				assertEquals("5",listLen('aaa,bbb,ccc,,',',',true));
				assertEquals("9",listLen('aaa,,,,,bbb,ccc,,',',',true));
				assertEquals("6",listLen('aaa,b.bb,c,.cc',',.',true));
				assertEquals("9",listLen('||a||b||c||','|',true));
				assertEquals("1",listLen("","",true));
			});

			it( title = "Checking with listlen member function", body = function( currentSpec ) {
				assertEquals("0",''.listLen());
				assertEquals("1",'aaa'.listLen());
				assertEquals("1",',,aaa,,'.listLen());
				assertEquals("3",'aaa,bbb,ccc'.listLen());
				assertEquals("3",'aaa,bbb,ccc,,'.listLen());
				assertEquals("3",'aaa,,,,,bbb,ccc,,'.listLen());
				assertEquals("5",'aaa,b.bb,c,.cc'.listLen(',.'));
				assertEquals("3",',,a,,b,,c,,'.listLen(',',false));
                assertEquals("1",''.listLen(',',true));
				assertEquals("5",',,aaa,,'.listLen(',',true));
				assertEquals("5",'aaa,bbb,ccc,,'.listLen(',',true));
				assertEquals("9",'aaa,,,,,bbb,ccc,,'.listLen(',',true));
				assertEquals("6",'aaa,b.bb,c,.cc'.listLen(',.',true));
				assertEquals("9",'||a||b||c||'.listLen('|',true));
				assertEquals("1","".listLen("",true));	
			});
		});
	}
}