component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ArraySort()", body=function() {
			it(title="checking ArraySort(asc;localeSensitive=false) function", body = function( currentSpec ) {
				var arr=['a','b'];
				arraySort(arr,'text','asc')
				assertEquals("a,b",arrayToList(arr));
			});
			it(title="checking ArraySort(desc;localeSensitive=false) function", body = function( currentSpec ) {
				var arr=['a','b'];
				arraySort(arr,'text','desc')
				assertEquals("b,a",arrayToList(arr));
			});

			it(title="checking ArraySort(asc;localeSensitive=true) function", body = function( currentSpec ) {
				var arr=['a','b'];
				arraySort(arr,'text','asc',true)
				writedump(arrayToList(arr)=="a,b");
				assertEquals("a,b",arrayToList(arr));
			});
			it(title="checking ArraySort(desc;localeSensitive=true) function", body = function( currentSpec ) {
				var arr=['a','b'];
				arraySort(arr,'text','desc',true)
				assertEquals("b,a",arrayToList(arr));
			});


			it(title="checking ArraySort() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				arr[1]=111;
				arr[2]=22;
				arr[3]=3;

				arr.sort("numeric");
				assertEquals("3" ,arr[1]);
				assertEquals("22" ,arr[2]);
				assertEquals("111" ,arr[3]);

				ArraySort(arr, "numeric");
				assertEquals("3", arr[1]);
				assertEquals("22", arr[2]);
				assertEquals("111", arr[3]);

				ArraySort(arr, "text");
				assertEquals("111", arr[1]);
				assertEquals("22", arr[2]);
				assertEquals("3", arr[3]);

				arr=arrayNew(1);
				arr[1]="BB";
				arr[2]="aa";
				arr[3]="bbb";

				try {
					ArraySort(arr, "numeric"); 
					fail("must throw:Non-numeric value found."); 
				} catch (any e){}

				ArraySort(arr, "text");
				assertEquals("BB",arr[1]);
				assertEquals("aa",arr[2]);
				assertEquals("bbb",arr[3]);

				ArraySort(arr, "textnocase");
				assertEquals("aa",arr[1]);
				assertEquals("BB",arr[2]);
				assertEquals("bbb",arr[3]);

				ArraySort(arr, "textnocase","asc");
				assertEquals("aa",arr[1]);
				assertEquals("BB",arr[2]);
				assertEquals("bbb",arr[3]);

				ArraySort(arr, "textnocase","desc");
				assertEquals("bbb",arr[1]);
				assertEquals("BB",arr[2]);
				assertEquals("aa",arr[3]);

				arr[4]=arrayNew(1);
				try{
					ArraySort(arr, "textnocase","desc");
					fail("must throw:In function ArraySort the array element at position 4 is not a simple value ");
				} catch(any e){}
					
				arr[4]="";

				try{
					ArraySort(arr, "susi");
					fail("must throw:Invalid sort type susi. ");
				} catch(any e) {}
					
				try{
					ArraySort(arr, "text","susi");
					fail("must throw:Invalid sort order susi. ");
				} catch(any e) {}
					
				arr=arrayNew(2);
				try{
					ArraySort(arr, "numeric");
					fail("must throw:The array passed cannot contain more than one dimension. ");
				} catch(any e) {}

				arr=listToArray("d,a,a,b,A");
				ArraySort(arr, "textnocase","desc");
				assertEquals("d,b,A,a,a",arrayToList(arr));

				arr=listToArray("d,a,a,b,A");
				ArraySort(arr, "textnocase","asc");
				assertEquals("a,a,A,b,d",arrayToList(arr));

				arr=["hello","world","susi","world"];
				// UDF
				arraySort(arr,doSort);
				assertEquals("hello,susi,world,world",arrayToList(arr));
				// Closure
				doSort=function (left,right){
					return Compare(left,right);
				};
				arraySort(arr,doSort);
				assertEquals("hello,susi,world,world",arrayToList(arr));

			});
		});
	}
	private function doSort(left,right){
		return Compare(left,right);
	}
}

