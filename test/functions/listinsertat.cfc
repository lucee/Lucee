component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title = "Test case for ListInsertAt", body = function() {
			it( title = 'Checking with ListInsertAt()', body = function( currentSpec ) {
				assertEquals("susi,aaa,bbb,ccc",ListInsertAt("aaa,bbb,ccc",1,"susi"));
				assertEquals(",,aaa,bbb,susi,ccc",ListInsertAt(',,aaa,bbb,ccc',3,'susi'));
				assertEquals(",,aaa,bbb,susi,ccc,,",ListInsertAt(',,aaa,bbb,ccc,,',3,'susi'));
				assertEquals("aaa,susi.bbb,ccc",ListInsertAt('aaa,bbb,ccc',2,'susi','.,;'));
				assertEquals("aaa,bbb,susi.ccc",ListInsertAt('aaa,bbb,ccc',3,'susi','.,;'));
				assertEquals(",,,,aaa,bbb,susi.ccc",ListInsertAt(',,,,aaa,bbb,ccc',3,'susi','.,;'));
				assertEquals(",;.eee.aaa,bbb.ccc;ddd,;..",ListInsertAt(',;.aaa,bbb.ccc;ddd,;..',1,'eee','.,;'));
				listOne = 'a,b.c';
				assertEquals("d;a,b.c",listone.ListInsertAt(1,'d',';,.:'));
				assertEquals("a,d;b.c",ListInsertAt('a,b.c',2,'d',';,.:'));
				assertEquals("a,b.d;c",ListInsertAt('a,b.c',3,'d',';,.:'));
				listTwo = 'aaa,,bbb,ccc';
				assertEquals("aaa,,bbb,susi,ccc",listTwo.listInsertAt(3,'susi',',',false));
				assertEquals("aaa,bbb,susi,ccc",ListInsertAt('aaa,bbb,ccc',3,'susi',',',false));
				assertEquals(",,,aaa,,bbb,susi,ccc",ListInsertAt(',,,aaa,,bbb,ccc',3,'susi',',',false));
				assertEquals(",,susi,,aaa,,bbb,ccc",ListInsertAt(',,,aaa,,bbb,ccc',3,'susi',',',true));
				assertEquals(",,,aaa,,susi,bbb,ccc",ListInsertAt(',,,aaa,,bbb,ccc',6,'susi',',',true));
			});
		});
	}
}