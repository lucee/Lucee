component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ){

		describe( title = "Test case for ListQualify()", body = function() {
			it( title = "Checking ListQualify()", body = function( currentSpec ) {
				assertEquals("",ListQualify('',':'));
				assertEquals(":1:",ListQualify('1',':',',','all'));
				assertEquals(":1:,:2:,:3:",ListQualify(',,1,2,3,',':',',','all'));
				assertEquals("{x}1{x},{x}2{x},{x}3{x}",ListQualify('1,2,3','{x}',',','all'));
				assertEquals("1,2,3,'a'",ListQualify('1,2,3,a','''',',','char'));
				assertEquals("''a'',''b'',''c''",ListQualify("'a','b','c'","'"));
				assertEquals("''a'',''b'',''c''",ListQualify("'a',,,,'b','c'","'"));
				var myList = "1,2,3,4,5,6,x";
				assertEquals("'1','2','3','4','5','6','x'",ListQualify(myList, "'"));
				assertEquals("'1','2','3','4','5','6','x'",ListQualify(myList, "'", ","));
				assertEquals("'1','2','3','4','5','6','x'",ListQualify(myList, "'", ",", "all"));
				assertEquals(":a:,:b:",ListQualify(',,a,,b,,',':',',','all',false));
				assertEquals("::,::,:a:,::,:b:,::,::",ListQualify(',,a,,b,,',':',',','all',true));
			});
		});
	}
}