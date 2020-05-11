component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {
		describe( title="Test suite for ListSort", body=function() {
			it( title='Test case for ListSort function  ',body=function( currentSpec ) {
				assertEquals("#ListSort('ccc,aaa,bbb,AAAA,BB','text','asc')#", "AAAA,BB,aaa,bbb,ccc");
				assertEquals("#ListSort('ccc,aaa,bbb,AAAA,BB','text','desc')#", "ccc,bbb,aaa,BB,AAAA");
				assertEquals("#ListSort('ccc,aaa,bbb,AAAA,BB','textnocase','asc')#", "aaa,AAAA,BB,bbb,ccc");
				assertEquals("#ListSort('ccc,aaa,bbb,AAAA,BB','textnocase','desc')#", "ccc,bbb,BB,AAAA,aaa");
				assertEquals("#ListSort('1111,3,44,777,2,11','textnocase','desc')#", "777,44,3,2,1111,11");
				assertEquals("#ListSort('1111,3,44,777,2,11','numeric','desc')#", "1111,777,44,11,3,2");
				assertEquals("#ListSort(',,,1111,3,44,777,2,11','numeric','desc')#", "1111,777,44,11,3,2");
				assertEquals("#ListSort(',,,111,,,1,3,44,777,2,11','numeric','desc')#", "777,111,44,11,3,2,1");
  				assertEquals("#ListSort(',,ccc,,,aaa,,,bbb,,,AAAA,,,BB,,','textnocase','asc',',',false)#", "aaa,AAAA,BB,bbb,ccc");
				assertEquals("#ListSort(',,1,,,4,,,9,,,8,,,3,,','numeric','asc',',',false)#", "1,3,4,8,9");

				list=("d,a,a,b,A");
				list=ListSort(list, "textnocase","desc");
				assertEquals("#list#", "d,b,A,a,a");

				list=("d,a,a,b,A");
				list=ListSort(list, "textnocase","asc");
				assertEquals("#list#", "a,a,A,b,d");
			});
		});
	}
}