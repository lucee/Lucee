component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ){
		describe( title = "Test case for listItemTrim", body = function() {
			it( title = "Checking with listItemTrim()", body = function( currentSpec ) {
				assertEquals(",a,b,c,d",listItemTrim(",,,,, , a ,, b ,c,d",','));
				assertEquals(",a,b,c,d",listItemTrim(",,,,, , a ,, b ,c,d",',',false));
				assertEquals(",,,,,,a,,b,c,d",listItemTrim(",,,,, , a ,, b ,c,d",',',true));
			});

			it( title = "Checking with string.listItemTrim()", body = function( currentSpec ){
				listOne = ",,,,, , a ,, b ,c,d";
				assertEquals(",,,,,,a,,b,c,d",listOne.listItemTrim(',',true));
				listTwo = ",,,,, , a ,, b ,c,d";
				assertEquals(",a,b,c,d",listTwo.listItemTrim(','));
			});
		});
	}
}