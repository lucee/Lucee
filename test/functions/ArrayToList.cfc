component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for arrayToList()", body=function() {
			it(title="Checking arrayToList() function", body = function( currentSpec ) {
				arr=arrayNew(1);
				arr[1]=111;
				arr[2]=22;
				arr[3]=3.5;

				assertEquals("111,22,3.5",arrayToList(arr));
				assertEquals("111223.5",arrayToList(arr,''));
				assertEquals("111,;22,;3.5",arrayToList(arr,',;'));

				arr[6]="ee";
				assertEquals("111,22,3.5,,,ee",arrayToList(arr));

				arr[7]="e,e";
				assertEquals("111,22,3.5,,,ee,e,e",arrayToList(arr));


				assertEquals("111;22;3.5;;;ee;e,e",arrayToList(arr,";"));

				arr=arrayNew(1);
				arr[1]="a";

				ArrayResize(arr, 10);
				assertEquals("a,,,,,,,,,",arrayToList(arr));

				arr=arrayNew(1);
				arr[1]="a";
				arr[2]="b";

				assertEquals("a{}b",arrayToList(arr,"{}"));

				arr=arrayNew(1);
				arr[4]=111;
				arr[5]=22;
				arr[6]=3.5;

				assertEquals(",,,111,22,3.5",arrayToList(arr));
			});

			it(title="Checking array.toList() member function", body = function( currentSpec ) {
				var arr=arrayNew(1);
				arr[1] = 111;
				arr[2] = 22;
				arr[3] = 3.5;

				assertEquals("111,22,3.5",arr.toList());
				assertEquals("111223.5",arr.toList(''));
				assertEquals("111,;22,;3.5",arr.toList(',;'));

				arr[6] = "ee";
				assertEquals("111,22,3.5,,,ee",arr.toList());

				arr[7] = "e,e";
				assertEquals("111,22,3.5,,,ee,e,e",arr.toList());

				assertEquals("111;22;3.5;;;ee;e,e",arr.toList(";"));

				arr=arrayNew(1);
				arr[1] = "a";

				ArrayResize(arr, 10);
				assertEquals("a,,,,,,,,,",arr.toList());

				arr=arrayNew(1);
				arr[1] = "a";
				arr[2] = "b";

				assertEquals("a{}b",arr.toList("{}"));

				arr=arrayNew(1);
				arr[4] = 111;
				arr[5] = 22;
				arr[6] = 3.5;

				assertEquals(",,,111,22,3.5",arr.toList());
			});
		});
	}
}