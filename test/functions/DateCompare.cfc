component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for DateCompare", function() {
			it(title="Checking with DateCompare", body=function( currentSpec ) {
				assertEquals("1" , "#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","d")#");
					
				assertEquals("1" , "#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","h")#");
					
				assertEquals("1" , "#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","n")#");
					
				assertEquals("0" , "#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","m")#");
					
				assertEquals("1" , "#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","s")#");
					
				assertEquals("1" , "#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 00:00:00'}","d")#");
					
				assertEquals("-1" , "#DateCompare("{ts '2007-10-09 00:00:00'}","{ts '2007-10-10 00:00:00'}","d")#");
				assertEquals("-1", "#dateCompare(1,2)#");

				d1 = CreateDateTime(2001, 11, 1, 4, 10, 1);
				d2 = CreateDateTime(2001, 11, 1, 4, 10, 4);
				assertEquals("-1" , "#DateCompare(d1, d2)#");
				assertEquals("-1" , "#DateCompare(d1, d2,"s")#");
				assertEquals("0" , "#DateCompare(d1, d2,"n")#");
				assertEquals("0" , "#DateCompare(d1, d2,"h")#");
				assertEquals("0" , "#DateCompare(d1, d2,"yyyy")#");
				d2 = CreateDateTime(2001, 11, 1, 5, 10, 4);
				assertEquals("0" , "#DateCompare(d1, d2,"m")#");
				assertEquals("0" , "#DateCompare(d1, d2,"d")#");
				assertEquals("0" , "#DateCompare(d1, d2,"d")#");
				try{
						assertEquals("0" , "#DateCompare(d1, d2,"w")#");
						fail("must throw:DateCompare w");
				}catch(any e) {}
				try{ 
						assertEquals("0", "#DateCompare(d1, d2,"ww")#");
						fail("must throw:DateCompare ww");
				}catch(any e) {}        
				try{ 
						assertEquals("0", "#DateCompare(d1, d2,"q")#");
						fail("must throw:DateCompare q");
				}catch(any e) {}
				try{
						assertEquals("0", "#DateCompare(d1, d2,"susi")#");
						fail("must throw:DateCompare susi");
				}catch(any e) {}

			});

			it(title="Checking with DateTime.Compare() member function", body=function( currentSpec ) {
				d1 = CreateDateTime(2001, 11, 1, 4, 10, 1);
				d2 = CreateDateTime(2001, 11, 1, 4, 10, 4);
				assertEquals("-1" , "#d1.compare(d2)#");
				assertEquals("-1" , "#d1.compare(d2,"s")#");
				assertEquals("0" , "#d1.compare(d2,"n")#");
				assertEquals("0" , "#d1.compare(d2,"h")#");
				assertEquals("0" , "#d1.compare(d2,"yyyy")#");
				d2 = CreateDateTime(2001, 11, 1, 5, 10, 4);
				assertEquals("0" , "#d1.compare(d2,"m")#");
				assertEquals("0" , "#d1.compare(d2,"d")#");
				assertEquals("0" , "#d1.compare(d2,"d")#");
			});
		});	
	}
}