component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for arraySplice", body = function() {

		////////////// 2 ARGUMENTS //////////////////
			it( title = '2 arguments',body = function( currentSpec ) {
			    var arr=['a','b','c'];
			    var res=ArraySplice(arr,2)
			    
				assertEquals("a",arrayToList(arr));
				assertEquals("b,c",arrayToList(res));
			});
			it( title = '2 arguments with null',body = function( currentSpec ) {
			    var arr=['a',nullValue(),'c'];
			    var res=ArraySplice(arr,2)
			    
				assertEquals("a",arrayToList(arr));
				assertEquals(",c",arrayToList(res));
			});


		////////////// 3 ARGUMENTS //////////////////
			it( title = '3 arguments (len within array)',body = function( currentSpec ) {
			    var arr=['a','b','c'];
			    var res=ArraySplice(arr,2,1)
			    
				assertEquals("a,c",arrayToList(arr));
				assertEquals("b",arrayToList(res));
			});
			it( title = '3 arguments (len 0)',body = function( currentSpec ) {
			    var arr=['a','b','c'];
			    var res=ArraySplice(arr,2,0)
			    
				assertEquals("a,b,c",arrayToList(arr));
				assertEquals("",arrayToList(res));
			});
			it( title = '3 arguments (len -1)',body = function( currentSpec ) {
			    var arr=['a','b','c'];
			    var res=ArraySplice(arr,2,-1)
			    
				assertEquals("a",arrayToList(arr));
				assertEquals("b,c",arrayToList(res));
			});
			it( title = '3 arguments (len -2)',body = function( currentSpec ) {
			    var arr=['a','b','c'];
			    var res=ArraySplice(arr,2,-2)
			    
				assertEquals("a,b,c",arrayToList(arr));
				assertEquals("",arrayToList(res));
			});

		////////////// 4 ARGUMENTS //////////////////
			it( title = '4 arguments ',body = function( currentSpec ) {
			    var arr=['a','b','c'];
			    var rep=['1','2'];
			    var res=ArraySplice(arr,2,1,rep)
			    
				assertEquals("a,1,2,c",arrayToList(arr));
				assertEquals("b",arrayToList(res));
			});
			it( title = '4 arguments with null',body = function( currentSpec ) {
			    var arr=['a',nullValue(),'c'];
				var rep=['1',nullValue(),'2'];
			    var res=ArraySplice(arr,2,1,rep)
			    
				assertEquals("a,1,,2,c",arrayToList(arr));
				assertEquals("",arrayToList(res));
			});

			
		});

	}
}